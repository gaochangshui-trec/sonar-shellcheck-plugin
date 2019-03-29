package com.emeraldsquad.sonar.plugin.shellcheck.issues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.emeraldsquad.sonar.plugin.shellcheck.TestUtils;
import com.emeraldsquad.sonar.plugin.shellcheck.languages.BashLanguage;

import org.apache.commons.io.FileUtils;
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
        TestUtils.addInputFile("src/test/resources/sensor/bad.sh", context, baseDir);
        IssuesFetcher fetcher = new TaskRunnerFetcher(context.fileSystem());
        String fetched = IOUtils.toString(fetcher.fetchReport());
        String expected = FileUtils.readFileToString(new File("src/test/resources/sensor/report.json"), StandardCharsets.UTF_8);
        assertEquals(fetched, expected);
    }

}