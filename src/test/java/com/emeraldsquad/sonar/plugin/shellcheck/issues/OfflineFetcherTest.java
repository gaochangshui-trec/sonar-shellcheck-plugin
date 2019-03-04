package com.emeraldsquad.sonar.plugin.shellcheck.issues;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * OfflineFetcherTest
 */
public class OfflineFetcherTest {

    @Test
    public void fetchAFile() throws Exception {
        IssuesFetcher fetcher = new OfflineFetcher("src/test/resources/sensor/report.json");
        Reader fetched = fetcher.fetchReport();
        Reader expected = new FileReader("src/test/resources/sensor/report.json");
        assertTrue(IOUtils.contentEquals(fetched, expected));
    }

}