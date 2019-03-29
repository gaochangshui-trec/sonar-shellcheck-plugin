package com.emeraldsquad.sonar.plugin.shellcheck.issues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.File;
import java.io.IOException;

import com.emeraldsquad.sonar.plugin.shellcheck.TestUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.LogTester;

public class BashIssuesLoaderSensorTest {

  @org.junit.Rule
  public final ExpectedException thrown = ExpectedException.none();

  @org.junit.Rule
  public LogTester logTester = new LogTester();

  private FileLinesContextFactory fileLinesContextFactory;

  private File baseDir = new File(new File("").getAbsolutePath());
  private SensorContextTester context = SensorContextTester.create(baseDir);

  private BashIssuesLoaderSensor createSensor(boolean withReport) {
    MapSettings settings = new MapSettings();
    if (withReport) {
      settings.setProperty(BashIssuesLoaderSensor.REPORT_PATH_KEY, "src/test/resources/sensor/allcases.json");
    }
    FileSystem fs = context.fileSystem();
    return new BashIssuesLoaderSensor(settings.asConfig(), fs);
  }

  @Before
  public void setUp() {
    fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
  }

  @Test
  public void should_contain_sensor_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();

    createSensor(true).describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("Shellcheck report importer");
    assertThat(descriptor.languages()).containsOnly("shell");
    assertThat(descriptor.type()).isEqualTo(Type.MAIN);
  }

  @Test
  public void should_analyse() throws IOException {
    TestUtils.addInputFile("src/test/resources/sensor/bad.sh", context, baseDir);
    createSensor(true).execute(context);
    assertThat(context.allIssues().size()).isEqualTo(4);
  }

  @Test
  public void should_run_and_analyse() throws IOException {
    TestUtils.addInputFile("src/test/resources/sensor/bad.sh", context, baseDir);
    createSensor(false).execute(context);
    assertThat(context.allIssues().size()).isEqualTo(3);
  }

}