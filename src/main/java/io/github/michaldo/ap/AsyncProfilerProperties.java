package io.github.michaldo.ap;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("async-profiler")
public class AsyncProfilerProperties {
    /**
     * Enable profiling.
     */
    private boolean enabled = false;

    /**
     * Profiling event: cpu, alloc, lock, cache-misses. For details see
     * https://github.com/async-profiler/async-profiler?tab=readme-ov-file#profiler-options
     */
    private String event = "wall,alloc";
    /**
     * Either a clock time (hh:mm:ss) or a loop duration in seconds, minutes, hours, or days.
     * Make sure the filename includes a timestamp pattern,
     * or the output will be overwritten on each iteration.
     * For details see https://github.com/async-profiler/async-profiler?tab=readme-ov-file#profiler-options
     *
     * Important: the property is not Duration is Spring format, but String in async-profiler format
     */
    private String loop = "5m";

    /**
     * File name to dump the profile information to.
     * %p in the file name is expanded to the PID of the target JVM;
     * %t - to the timestamp;
     * %n{MAX} - to the sequence number;
     * %{ENV} - to the value of the given environment variable.
     * examples:
     * persistent-volume/%t-${spring.application.name}.jfr
     * persistent-volume/%t-${env.HOSTNAME}.jfr
     * persistent-volume/%t-%{HOSTNAME}.jfr
     */
    private String file = "%t.jfr";

    /**
     * How long keep dump files
     */
    private Duration maxDumpAge = Duration.ofHours(24);

    /**
     * Profiling interval in nanoseconds or in other units,
     * if N is followed by ms (for milliseconds),
     * us (for microseconds), or s (for seconds).
     * Only CPU active time is counted. No samples are collected while CPU is idle.
     */
    private String interval = "100ms";

    public boolean isEnabled() {
        return enabled;
    }

    public String getEvent() {
        return event;
    }

    public String getFile() {
        return file;
    }

    public String getLoop() {
        return loop;
    }

    public Duration getMaxDumpAge() {
        return maxDumpAge;
    }


    public void setEvent(String event) {
        this.event = event;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setLoop(String loop) {
        this.loop = loop;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMaxDumpAge(Duration maxDumpAge) {
        this.maxDumpAge = maxDumpAge;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }
}
