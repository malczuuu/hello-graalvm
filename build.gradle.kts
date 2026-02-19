import java.nio.charset.Charset
import java.nio.file.Files

import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask

plugins {
    id("application")
    id("java")
    id("org.graalvm.buildtools.native").version("0.11.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.problem4j:problem4j-core:1.4.1")

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

application {
    mainClass = "io.github.malczuuu.sandbox.graalvm.Main"
}

graalvmNative {
    toolchainDetection = false
    binaries {
        named("main") {
            imageName = "hello-graalvm"
            mainClass = "io.github.malczuuu.sandbox.graalvm.Main"

            javaLauncher = javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(25)
                vendor = JvmVendorSpec.GRAAL_VM
            }
        }
    }
}

tasks.register<JavaExec>("runWithAgent") {
    group = "native-image"
    description = "Run the app with GraalVM native-image agent to generate META-INF/native-image."

    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.github.malczuuu.sandbox.graalvm.Main"

    jvmArgs = listOf(
        "-agentlib:native-image-agent=config-output-dir=${projectDir}/src/main/resources/META-INF/native-image"
    )
}

// Gradle's "Copy" task cannot handle symbolic links, see https://github.com/gradle/gradle/issues/3982. That is why
// links contained in the GraalVM distribution archive get broken during provisioning and are replaced by empty
// files. Address this by recreating the links in the toolchain directory.
//
// See:
// https://github.com/oss-review-toolkit/ort/blob/06059ddd268a9e58c2aaab203ed293f47aa94d3b/buildSrc/src/main/kotlin/ort-application-conventions.gradle.kts#L137-L165
tasks.named<BuildNativeImageTask>("nativeCompile") {
    val toolchainDir = options.get().javaLauncher.get().executablePath.asFile.parentFile.run {
        if (name == "bin") parentFile else this
    }

    val toolchainFiles = toolchainDir.walkTopDown().filter { it.isFile }
    val emptyFiles = toolchainFiles.filter { it.length() == 0L }

    // Find empty toolchain files that are named like other toolchain files and assume these should have been links.
    val links = toolchainFiles.mapNotNull { file ->
        emptyFiles.singleOrNull { it != file && it.name == file.name }?.let {
            file to it
        }
    }

    // Fix up symbolic links.
    links.forEach { (target, link) ->
        logger.quiet("Fixing up '$link' to link to '$target'.")

        if (link.delete()) {
            Files.createSymbolicLink(link.toPath(), target.toPath())
        } else {
            logger.warn("Unable to delete '$link'.")
        }
    }
}
