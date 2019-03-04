package com.emeraldsquad.sonar.plugin.shellcheck.issues;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import com.emeraldsquad.sonar.plugin.shellcheck.languages.BashLanguage;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

/**
 * TaskRunnerFetcherTest
 */
public class TaskRunnerFetcherTest {

    private File baseDir = new File(new File("").getAbsolutePath());
    private SensorContextTester context = SensorContextTester.create(baseDir);

    @Test
    public void fetchAFile() throws Exception {
        addInputFile("src/test/resources/sensor/bad.sh");
        IssuesFetcher fetcher = new TaskRunnerFetcher(context.fileSystem());
        Reader fetched = fetcher.fetchReport();
        Reader expected = new FileReader("src/test/resources/sensor/report.json");
        assertTrue(IOUtils.contentEquals(fetched, expected));
    }


    private void addInputFile(String relativePath) {
        DefaultInputFile inputFile = new TestInputFileBuilder("moduleKey", relativePath)
          .setModuleBaseDir(baseDir.toPath())
          .setType(Type.MAIN)
          .setLanguage(BashLanguage.KEY)
          .setCharset(StandardCharsets.UTF_8)
          .build();
    
        context.fileSystem().add(inputFile);
    }
}