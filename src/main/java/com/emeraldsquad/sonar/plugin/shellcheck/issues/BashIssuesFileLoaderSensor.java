package com.emeraldsquad.sonar.plugin.shellcheck.issues;

import com.emeraldsquad.sonar.plugin.shellcheck.languages.BashLanguage;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

/**
 * Created by Thomas Detoux on 11/2/18.
 */
public class BashIssuesFileLoaderSensor extends BashIssuesLoaderSensor {
    private static final Logger LOGGER = Loggers.get(BashIssuesFileLoaderSensor.class);

    /**
     * Use of IoC to get Settings, FileSystem, RuleFinder and ResourcePerspectives
     *
     * @param config
     * @param fileSystem
     */
    public BashIssuesFileLoaderSensor(Configuration config, FileSystem fileSystem) {
        super(config, fileSystem);
    }

    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.name("Bash Issues Loader Sensor");
        descriptor.onlyOnLanguage(BashLanguage.KEY);
    }

    @Override
    protected void doExecute(String reportPath) {
        if (reportPath != null) {
            File analysisResultsFile = new File(reportPath);
            LOGGER.info("Parsing file {}", analysisResultsFile.getAbsolutePath());
            try (Reader shellCheckResult = new FileReader(analysisResultsFile)) {
                parseAndSaveResults(shellCheckResult);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to parse the provided bash file", e);
            }
        }

    }
}
