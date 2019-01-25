package com.emeraldsquad.sonar.plugin.shellcheck.issues;

import com.emeraldsquad.sonar.plugin.shellcheck.languages.BashLanguage;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Thomas Detoux on 11/2/18.
 */
public class BashIssueRunLoaderSensor extends BashIssuesLoaderSensor {

    private static final Logger LOGGER = Loggers.get(BashIssueRunLoaderSensor.class);

    private static final String URL_LINUX = "https://shellcheck.storage.googleapis.com/shellcheck-v0.5.0.linux.x86_64.tar.xz";
    private static final String SHA_LINUX = "475e14bf2705ad4a16d405fa64b94c2eb151a914d5a165ce13e8f9344e6145893f685a650cd32d45a7ab236dedf55f76b31db82e2ef76ad6175a87dd89109790";

    private static final String URL_WINDOWS = "https://shellcheck.storage.googleapis.com/shellcheck-v0.5.0.zip";
    private static final String SHA_WINDOWS = "5e1cd46b052f5669a021cd308daacf1221fd314964ceb96fd494c806e7620eba86bc32dcf5e3b9959904b374cce809b90e43fbc4df7e0f6289adf42ea1956465";

    /**
     * Use of IoC to get Settings, FileSystem, RuleFinder and ResourcePerspectives
     *
     * @param config
     * @param fileSystem
     */
    public BashIssueRunLoaderSensor(Configuration config, FileSystem fileSystem) {
        super(config, fileSystem);
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("ShellCheck executor Sensor");
        descriptor.onlyOnLanguage(BashLanguage.KEY);
    }


    @Override
    protected void doExecute(String reportPath) {
        if (reportPath != null) {
            return;
        }

        FileSystem fileSystem = context.fileSystem();
        FilePredicate filePredicate = fileSystem.predicates().and(
                fileSystem.predicates().hasType(InputFile.Type.MAIN),
                fileSystem.predicates().hasLanguage(BashLanguage.KEY)
        );


        if (fileSystem.hasFiles(filePredicate)) try {

            String shellCheckExe = downloadExe();

            List<String> command = new ArrayList<>();
            command.add(shellCheckExe);
            command.add("-f");
            command.add("json");

            fileSystem.inputFiles(filePredicate).forEach((InputFile f) -> {
                Path baseDir = fileSystem.baseDir().toPath();
                Path file = baseDir.resolve(f.uri().getPath());
                command.add(baseDir.relativize(file).toString());
            });

            LOGGER.info("Starting process : {}", String.join(" ", command));

            executeShellCheck(command);

        } catch (Exception e){
            LOGGER.error("Error downloading and executing shellcheck", e);
        }

    }

    private void executeShellCheck(List<String> command) throws IOException, InterruptedException {
        Process  process = new ProcessBuilder(command).start();
        if (process.waitFor() <= 1) {
            LOGGER.info("shellcheck terminated successfully");
            try (Reader reader = new InputStreamReader(process.getInputStream())) {
                parseAndSaveResults(reader);
            } catch (Exception e) {
                LOGGER.error("Unable to parse shellcheck result", e);
            }
        } else {
            try {
                LOGGER.error("An error occured during the execution of shellcheck: \n{}", IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                LOGGER.error("An error occured during the execution of shellcheck and the following exception was thrown while retrieving the error message", e);
            }
        }
    }

    private InputStream uncompressArchive(File archive, String url) throws FileNotFoundException, CompressorException {
        if (url.endsWith(".xz")) {
            return new CompressorStreamFactory().createCompressorInputStream(
                    new BufferedInputStream(new FileInputStream(archive))
            );
        } else {
            return new FileInputStream(archive);
        }
    }

    private String downloadExe() throws IOException, ArchiveException, CompressorException {
        String url;
        String expectedSha;

        if (SystemUtils.IS_OS_LINUX) {
            url = URL_LINUX;
            expectedSha = SHA_LINUX;
        } else {
            url = URL_WINDOWS;
            expectedSha = SHA_WINDOWS;
        }

        LOGGER.info("Downloading file {}", url);
        File archive = File.createTempFile("shellCheck", ".z");
        archive.deleteOnExit();

        FileUtils.copyURLToFile(new URL(url), archive);

        LOGGER.info("Checking checksum");
        try (FileInputStream archiveInput = new FileInputStream(archive)) {
            String calculatedSha = DigestUtils.sha512Hex(archiveInput);
            if (!expectedSha.equals(calculatedSha)) {
                throw new RejectedExecutionException("The downloaded shellcheck binary does not match the expected sha512sum");
            }
        }

        LOGGER.info("Extracting binary");
        File exe = File.createTempFile("shellCheck", ".exe");
        exe.deleteOnExit();

        try (ArchiveInputStream archiveInput = new ArchiveStreamFactory().createArchiveInputStream(
                new BufferedInputStream(uncompressArchive(archive, url)))) {
            ArchiveEntry entry = null;
            boolean foundExe = false;
            while ((entry = archiveInput.getNextEntry()) != null) {
                if (!entry.getName().endsWith(".txt")) {
                    FileUtils.copyInputStreamToFile(archiveInput, exe);
                    foundExe = true;
                    break;
                }
            }
            if (!foundExe) {
                throw new IllegalStateException("The downloaded archive does not contains the shellcheck binary");
            }
        }

        if (!exe.setExecutable(true)) {
            throw new RejectedExecutionException("Failed to add execution permission to shellCheck binary");
        }

        return exe.getCanonicalPath();
    }

}
