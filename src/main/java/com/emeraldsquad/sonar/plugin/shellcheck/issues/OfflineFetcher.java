package com.emeraldsquad.sonar.plugin.shellcheck.issues;

import java.io.FileReader;
import java.io.Reader;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class OfflineFetcher implements IssuesFetcher {
    private static final Logger LOGGER = Loggers.get(OfflineFetcher.class);
    private final String reportPath;

    /**
     * Use of IoC to get Settings, FileSystem, RuleFinder and ResourcePerspectives
     *
     * @param config
     * @param fileSystem
     */
    public OfflineFetcher(String reportPath) {
        this.reportPath = reportPath;
    }

    @Override
    public Reader fetchReport() throws Exception {
            LOGGER.info("Parsing file {}", reportPath);
            return new FileReader(reportPath);
    }
}
