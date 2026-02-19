# Hello, GraalVM!

This project demonstrates how reflection access to unused fields works in GraalVM. Because unreachable code and fields
are stripped from the final native binary, it is necessary to include proper `resources/META-INF/native-image` config
for each class that may be accessed reflectively.

## Generating `META-INF/native-image` metadata

You can automatically generate the required metadata using the GraalVM `native-image` agent. See `build.gradle.kts`.

```kotlin
tasks.register<JavaExec>("runWithAgent") {
    group = "native-image"
    description = "Run the app with GraalVM native-image agent to generate META-INF/native-image metadata."

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.malczuuu.sandbox.graalvm.Main")

    jvmArgs = listOf(
        "-agentlib:native-image-agent=config-output-dir=${projectDir}/src/main/resources/META-INF/native-image"
    )
}
```

### How it works

- The task runs your application with the **GraalVM native-image agent** attached.
- The agent monitors all reflective operations (field access, method calls, etc.) during runtime.
- It automatically generates or updates the metadata files in `resources/META-INF/native-image`, such as:
    - `reachability-metadata.json`,
    - (various other can be generated but are irrelevant for this scenario).

### Usage

Run the task from Gradle:

```bash
./gradlew runWithAgent
```

After execution, check the directory:

```
src/main/resources/META-INF/native-image
```

You will find the updated metadata files, which ensure that all classes and fields accessed reflectively are preserved 
in the native image.

### Notes

- Only code and fields actually **used during the run** are registered. Make sure to exercise all code paths that
  require reflection.
- You can also create similar tasks for **unit tests** to capture reflective access in test scenarios.
- Once metadata is generated, building the native image with:

```bash
./gradlew nativeCompile
```

will include all necessary reflection information, preventing `null` values or missing fields in the native binary.
