[![Maven Central](https://img.shields.io/maven-central/v/io.github.michaldo/michaldo-ap-spring-boot-starter.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.michaldo/michaldo-ap-spring-boot-starter)


# Michaldo Async Profiler Spring Boot Starter

Async Profiler https://github.com/async-profiler/async-profiler is packed into Loader
for Async Profiler https://github.com/jvm-profiling-tools/ap-loader and automatically
started by Spring Boot application

Async Profiler works only on Unix and MacOS.

# Usage scenario

When Async Profiler is enabled, collects profiling information and periodically dump
to files. Dump files are inspected to track problems. For example, Ultimate Intellij
handle dump files.

# Parameters

| Name                        | Meaning             | Default value | Async Profiler native 
|-----------------------------|---------------------|---------------|----------------------
| async-profiler.enabled      | Enable profiling.   | false         | no
| async-profiler.event        | Profiling mode      | wall,alloc    | yes
| async-profiler.interval     | Profiling interval  | 100ms         | yes
| async-profiler.file         | Dump file template  | %t.jfr        | yes
| async-profiler.loop         | Dump file frequency | 5m            | yes
| async-profiler.max-dump-age | Dump file max age   | 24h           | no

Native Async Profiler parameters
are fully documented here: https://github.com/async-profiler/async-profiler?tab=readme-ov-file#profiler-options

# Alternatives

This starter is inspired https://github.com/krzysztofslusarski/continuous-async-profiler
Continuous started is more advanced, but also more complicated to manage and maintain.
Direct reason why I created own started is that continuous one does not support profiling
more than one event, for example wall and alloc