package com.emeraldsquad.sonar.plugin.shellcheck.measures;

import com.emeraldsquad.sonar.plugin.shellcheck.languages.BashLanguage;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LineOfCodeSensor implements Sensor {

    private static final Logger log = Loggers.get(LineOfCodeSensor.class);

    enum CounterType {
        COMMENT,
        CODE,
        FUNCTION;
    }

    @Override
    public void describe(SensorDescriptor sensorDescriptor) {
        sensorDescriptor.name("Shell lines of codes sensor")
            .onlyOnLanguage(BashLanguage.KEY)
            .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext context) {
        FileSystem fileSystem = context.fileSystem();
        FilePredicate filePredicate = fileSystem.predicates().and(
                fileSystem.predicates().hasType(InputFile.Type.MAIN),
                fileSystem.predicates().hasLanguage(BashLanguage.KEY)
        );

        for (InputFile inputFile: fileSystem.inputFiles(filePredicate)) {
            this.analyseFile(inputFile, context);
        }

    }

    private void analyseFile(InputFile inputFile, SensorContext context) {
        log.debug("Analysing file : {}", inputFile);

        Map<CounterType, AtomicInteger> lines = countLines(inputFile);

        context.<Integer>newMeasure().on(inputFile)
                .withValue(lines.get(CounterType.CODE).get())
                .forMetric(CoreMetrics.NCLOC).save();

        context.<Integer>newMeasure().on(inputFile)
                .withValue(lines.get(CounterType.COMMENT).get())
                .forMetric(CoreMetrics.COMMENT_LINES).save();

        context.<Integer>newMeasure().on(inputFile)
                .withValue(lines.get(CounterType.FUNCTION).get())
                .forMetric(CoreMetrics.FUNCTIONS).save();
    }

    private Map<CounterType,AtomicInteger> countLines(InputFile file) {

        Map<CounterType, AtomicInteger> counters = new EnumMap<>(CounterType.class);
        counters.put(CounterType.CODE, new AtomicInteger());
        counters.put(CounterType.COMMENT, new AtomicInteger());
        counters.put(CounterType.FUNCTION, new AtomicInteger());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.inputStream()))) {
            reader.lines()
                    .filter(line -> !line.matches("^\\s*$"))
                    .forEach(line -> {
                        if (line.matches("\\s*\\#.*")) {
                            counters.get(CounterType.COMMENT).getAndIncrement();
                        } else {
                            counters.get(CounterType.CODE).getAndIncrement();
                            if (line.matches("^\\s*\\w+\\s*\\(\\s*\\)\\s*\\{?")
                                    || line.matches("^\\s*function\\s+\\w+\\s*\\{?")) {
                                counters.get(CounterType.FUNCTION).getAndIncrement();
                            }
                        }
                    });
        } catch (IOException e) {
            log.error(String.format("Error during analysis of file '%s': '%s'", file,
                    e.getMessage()), e);
        }

        return counters;
    }
}
