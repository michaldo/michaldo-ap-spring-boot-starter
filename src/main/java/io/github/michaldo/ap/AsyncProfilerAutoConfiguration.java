package io.github.michaldo.ap;

import one.profiler.AsyncProfilerLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Starts async profiler and dump file cleanup.
 *
 * Async profiler runs in loop mode. Profiled information periodically dumped to file and reset.
 * It is important to include timestamp in file pattern - otherwise file will be overwritten each dump.
 * File is created when profile loop starts - timestamp is begin of loop.
 *
 * In microservices environment different instance should dump to different files.
 * Uniques policy specific to the environment. For example for Kubernetes environment variable
 * HOSTNAME is unique per pod.
 *
 * Dumps are large, for example 15MB per 5 minutes. Cleaning is a must. To simplify,
 * self-cleaning is implemented.
 *
 * If max dump age is more or equal 1 hour, cleaning is performed once an hour.
 * If max dump age is less than 1 hour, it is recognized as experiment and cleaning is performed
 * once a minute
 */
@AutoConfiguration
@EnableConfigurationProperties(AsyncProfilerProperties.class)
@ConditionalOnProperty("async-profiler.enabled")
public class AsyncProfilerAutoConfiguration  {

    private static final Log log = LogFactory.getLog(AsyncProfilerAutoConfiguration.class);

    @Autowired void start(AsyncProfilerProperties props) throws IOException {
        startAsyncProfiler(props);
        startCleanup(props);
    }

    private void startAsyncProfiler(AsyncProfilerProperties props) throws IOException {
        Path directories = Paths.get(props.getFile()).getParent();
        if (directories != null) {
            Files.createDirectories(directories);
        }
        String command = String.format(
                "start,event=%s,loop=%s,interval=%s,file=%s",
                props.getEvent(),
                props.getLoop(),
                props.getInterval(),
                props.getFile());
        AsyncProfilerLoader.load().execute(command);
        log.info("AsyncProfiler started with command: " + command);
    }

    private void startCleanup(AsyncProfilerProperties props) {
        Path dumpDirectory = Paths.get(props.getFile()).getParent();
        if (dumpDirectory == null) {
            dumpDirectory = Paths.get(".");
        }
        String dumpExtension = extension(props.getFile());
        long initialDelay = 2;
        long period = 60;
        TimeUnit unit = TimeUnit.MINUTES;
        if (props.getMaxDumpAge().compareTo(Duration.ofHours(1)) < 0) {
            // keep output less than 1 hour is experiment
            unit = TimeUnit.SECONDS;
        }
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                deleteOldDump(dumpDirectory, dumpExtension, props.getMaxDumpAge()), initialDelay, period, unit);
        String pseudoCommand = "rm " + dumpDirectory + "/*" + dumpExtension
                + " -mtime " + valueOf(props.getMaxDumpAge())
                + " every " + period + " " + unit.toString().toLowerCase();
        log.info("Profiler cleanup started: " + pseudoCommand);
    }

    private Runnable deleteOldDump(Path directory, String extension, Duration maxFileAge) {

        FilterPredicate isDump = path -> path.toString().endsWith(extension);
        FilterPredicate isOld = path ->
                Files.getLastModifiedTime(path)
                        .compareTo(
                                FileTime.from(Instant.now().minus(maxFileAge))) < 0;
        FilterPredicate isFile = Files::isRegularFile;

        FilterPredicate deleteFilter = isDump.and(isOld).and(isFile);

        return () -> {

            try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, deleteFilter)) {
                int counter = 0;
                for (Path filePath : files) {
                    Files.delete(filePath);
                    counter++;
                }
                if (counter > 0) {
                    log.info("Deleted " + counter + " dump file(s) from '" + directory + "'");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private String extension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        } else {
            throw new IllegalArgumentException("async-profiler.file must use extension, " +
                    "for example '%t.jfr', but current is " + fileName);
        }
    }

    private interface FilterPredicate extends DirectoryStream.Filter<Path> {

        default FilterPredicate and(FilterPredicate other) {
            return path -> accept(path) && other.accept(path);
        }
    }

    // example: 1d0h0m
    private String valueOf(Duration duration) {
        return String.format("%dd%dh%dm",
                duration.toDays(), duration.toHoursPart(), duration.toMinutesPart());
    }
}