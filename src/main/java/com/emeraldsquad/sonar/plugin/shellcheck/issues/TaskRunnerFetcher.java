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
import org.apache.commons.lang3.SystemUtils;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class TaskRunnerFetcher implements IssuesFetcher {

    private static final Logger LOGGER = Loggers.get(TaskRunnerFetcher.class);

    private static final String URL_LINUX = "https://shellcheck.storage.googleapis.com/shellcheck-v0.7.0.linux.x86_64.tar.xz";
    private static final String SHA_LINUX = "84e06bee3c8b8c25f46906350fb32708f4b661636c04e55bd19cdd1071265112d84906055372149678d37f09a1667019488c62a0561b81fe6a6b45ad4fae4ac0";

    private static final String URL_WINDOWS = "https://shellcheck.storage.googleapis.com/shellcheck-v0.7.0.zip";
    private static final String SHA_WINDOWS = "10ee2474845eeb76d8a13992457472b723edf470c7cf182a20b32ecee4ad009ec6b2ca542db8f66127cf19e24baf3a06838a0d101494a5a6c11b3b568f9f5a99";

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
