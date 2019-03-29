package com.emeraldsquad.sonar.plugin.shellcheck.issues;

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
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class TaskRunnerFetcher implements IssuesFetcher {

    private static final Logger LOGGER = Loggers.get(TaskRunnerFetcher.class);

    private static final String URL_LINUX = "https://shellcheck.storage.googleapis.com/shellcheck-v0.6.0.linux.x86_64.tar.xz";
    private static final String SHA_LINUX = "d88733e95aea8e970c373a3f677a3eb272f14c12d3e9c93f81463b5fe406b43acdd3046d10c092f40c070a96a5fac1cf7e18b35ed790d76ecced6af32e2c8a85";

    private static final String URL_WINDOWS = "https://shellcheck.storage.googleapis.com/shellcheck-v0.6.0.zip";
    private static final String SHA_WINDOWS = "2d8171e79cafeeaefd1dc3be30cf44f8c3f2df0f18f6b54d4a028ab26c5159e07c9a1f0bc67603d1ae52bc2ba8d337df596079fec28bf1258255956bb552ce53";

    private final FileSystem fileSystem;

    public TaskRunnerFetcher(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public Reader fetchReport() throws Exception {
        FilePredicate filePredicate = fileSystem.predicates().and(
                fileSystem.predicates().hasType(InputFile.Type.MAIN),
                fileSystem.predicates().hasLanguage(BashLanguage.KEY)
        );

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

        return executeShellCheck(command);
    }

    private Reader executeShellCheck(List<String> command) throws IOException, InterruptedException {
        Process  process = new ProcessBuilder(command).start();
        if (process.waitFor() <= 1) {
            LOGGER.info("shellcheck terminated successfully");
            return new InputStreamReader(process.getInputStream());
        } else {
            throw new RuntimeException("An error occured during the execution of shellcheck: \n" + IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8));
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
