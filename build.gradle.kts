plugins {
    id("application")
    id("java")
    id("org.graalvm.buildtools.native").version("0.11.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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
                languageVersion = JavaLanguageVersion.of(21)
                vendor = JvmVendorSpec.GRAAL_VM
            }
        }
    }
}
