/*
 * Example Plugin for SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.emeraldsquad.sonar.plugin.shellcheck.issues;

import com.emeraldsquad.sonar.plugin.shellcheck.rules.BashRulesDefinition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

/**
 * The goal of this Sensor is to load the results of an analysis performed by a fictive external tool named: FooLint
 * Results are provided as an xml file and are corresponding to the rules defined in 'rules.xml'.
 * To be very abstract, these rules are applied on source files made with the fictive language Foo.
 */
public abstract class BashIssuesLoaderSensor implements Sensor {

  private static final Logger LOGGER = Loggers.get(BashIssuesLoaderSensor.class);

  protected static final String REPORT_PATH_KEY = "sonar.bash.reportPath";

  protected final Configuration config;
  protected final FileSystem fileSystem;
  protected SensorContext context;

  /**
   * Use of IoC to get Settings, FileSystem, RuleFinder and ResourcePerspectives
   */
  public BashIssuesLoaderSensor(final Configuration config, final FileSystem fileSystem) {
    this.config = config;
    this.fileSystem = fileSystem;
  }

  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  protected String getReportPath() {
    Optional<String> o = config.get(reportPathKey());
    if (o.isPresent()) {
      return o.get();
    }
    return null;
  }

  @Override
  public void execute(final SensorContext context) {
      this.context = context;
    String reportPath = getReportPath();
    doExecute(reportPath);
  }

  protected abstract void doExecute(String reportPath);

  protected void parseAndSaveResults(final Reader shellCheckResult) {
    LOGGER.info("Parsing 'Shellcheck' Analysis Results");
    BashAnalysisResultsParser parser = new BashAnalysisResultsParser();
    Collection<ErrorDataFromExternalLinter> errors = parser.parse(shellCheckResult);
    for (ErrorDataFromExternalLinter error : errors) {
      getResourceAndSaveIssue(error);
    }
  }

  private void getResourceAndSaveIssue(final ErrorDataFromExternalLinter error) {
    LOGGER.debug(error.toString());

    InputFile inputFile = fileSystem.inputFile(
      fileSystem.predicates().and(
        fileSystem.predicates().hasRelativePath(error.getFile()),
        fileSystem.predicates().hasType(InputFile.Type.MAIN)));

    LOGGER.debug("inputFile null ? " + (inputFile == null));

    Severity severity = null;
    switch (error.getLevel()) {
        case "info": severity = Severity.INFO;
             break;
        case "style": severity = Severity.MINOR;
             break;
        case "warning": severity = Severity.MAJOR;
             break;
        case "error": severity = Severity.CRITICAL;
             break;
        default: severity = Severity.MINOR;
    }

    if (inputFile != null) {
        String ruleKey = "SC" + error.getCode();
        saveIssue(inputFile, error.getLine(), error.getColumn(), ruleKey, severity, error.getMessage());
    } else {
      LOGGER.error("Not able to find a InputFile with " + error.getFile());
    }
  }

  private void saveIssue(final InputFile inputFile, int line, int column, final String externalRuleKey, Severity severity, final String message) {
    String languageKey = inputFile.language();

    if (languageKey != null) {

      RuleKey ruleKey = RuleKey.of(getRepositoryKeyForLanguage(languageKey), externalRuleKey);

      NewIssue newIssue = context.newIssue()
              .forRule(ruleKey);

      NewIssueLocation primaryLocation = newIssue.newLocation()
              .on(inputFile)
              .message(message);

      if (line > 0 && column > 0) {
        primaryLocation.at(inputFile.newRange(line, column - 1, line, column));
      } else if (line > 0) {
        primaryLocation.at(inputFile.selectLine(line));
      }

      newIssue.at(primaryLocation);

      if (severity != null) {
        newIssue.overrideSeverity(severity);
      }

      newIssue.save();
    }
  }

  private static String getRepositoryKeyForLanguage(String languageKey) {
    return languageKey.toLowerCase() + "-" + BashRulesDefinition.KEY;
  }

  @Override
  public String toString() {
    return "BashIssuesLoaderSensor";
  }

  private class ErrorDataFromExternalLinter {

    private String file;
    private int line;
    private int column;
    private String level;
    private int code;
    private String message;

    public ErrorDataFromExternalLinter(String file, int line, int column, String level, int code, String message) {
      this.file = file;
      this.line = line;
      this.column = column;
      this.level = level;
      this.code = code;
      this.message = message;
    }

    public String getFile() {
      return file;
    }

    public int getLine() {
      return line;
    }

    public int getColumn() {
      return column;
    }

    public String getLevel() {
      return level;
    }

    public int getCode() {
      return code;
    }

    public String getMessage() {
      return message;
    }

    @Override
    public String toString() {
      return "ErrorDataFromExternalLinter{" +
              "file='" + file + '\'' +
              ", line=" + line +
              ", column=" + column +
              ", level='" + level + '\'' +
              ", code=" + code +
              ", message='" + message + '\'' +
              '}';
    }
  }

  private class BashAnalysisResultsParser {

    public Collection<ErrorDataFromExternalLinter> parse(final Reader shellCheckResult) {
        Gson gson = new Gson();
        Type errorListType = new TypeToken<Collection<ErrorDataFromExternalLinter>>() {
        }.getType();

        return gson.fromJson(shellCheckResult, errorListType);
    }
  }

}
