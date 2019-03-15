package com.emeraldsquad.sonar.plugin.shellcheck;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.emeraldsquad.sonar.plugin.shellcheck.languages.BashLanguage;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

/**
 * TestUtils
 */
public class TestUtils {

    public static void addInputFile(String relativePath, SensorContextTester context, File baseDir) throws IOException {
        DefaultInputFile inputFile = new TestInputFileBuilder("moduleKey", relativePath)
          .setModuleBaseDir(baseDir.toPath())
          .setType(Type.MAIN)
          .setLanguage(BashLanguage.KEY)
          .setCharset(StandardCharsets.UTF_8)
          .setContents(FileUtils.readFileToString(new File(relativePath), StandardCharsets.UTF_8))
          .build();

        
    
        context.fileSystem().add(inputFile);
    }
}