package com.emeraldsquad.sonar.plugin.shellcheck.measures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

import com.emeraldsquad.sonar.plugin.shellcheck.languages.BashLanguage;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.LogTester;

public class LineOfCodeSensorTest {

  @org.junit.Rule
  public final ExpectedException thrown = ExpectedException.none();

  @org.junit.Rule
  public LogTester logTester = new LogTester();

  private FileLinesContextFactory fileLinesContextFactory;
  
  private File baseDir = new File("src/test/resources");
    private SensorContextTester context = SensorContextTester.create(baseDir);

  private LineOfCodeSensor createSensor() {
    return new LineOfCodeSensor();
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

    createSensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("Shell lines of codes sensor");
    assertThat(descriptor.languages()).containsOnly("shell");
    assertThat(descriptor.type()).isEqualTo(Type.MAIN);
  }

  @Test
  public void should_analyse() {
    String relativePath = "sensor/loc.sh";
    inputFile(relativePath);

    createSensor().execute(context);

    String key = "moduleKey:" + relativePath;

    assertThat(context.measure(key, CoreMetrics.NCLOC).value()).isEqualTo(36);
    assertThat(context.measure(key, CoreMetrics.FUNCTIONS).value()).isEqualTo(10);
    assertThat(context.measure(key, CoreMetrics.COMMENT_LINES).value()).isEqualTo(2);
  }

  private InputFile inputFile(String relativePath) {
    DefaultInputFile inputFile = new TestInputFileBuilder("moduleKey", relativePath)
      .setModuleBaseDir(baseDir.toPath())
      .setType(Type.MAIN)
      .setLanguage(BashLanguage.KEY)
      .setCharset(StandardCharsets.UTF_8)
      .build();

    context.fileSystem().add(inputFile);

    try {
      inputFile.setMetadata(new FileMetadata().readMetadata(new FileInputStream(inputFile.file()), StandardCharsets.UTF_8, inputFile.absolutePath()));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return inputFile;
}

}