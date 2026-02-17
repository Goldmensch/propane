package project.convention

import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

java {
    val versionString = properties["java.version"] as String
    val version = JavaVersion.valueOf("VERSION_${versionString}")

    targetCompatibility = version
    sourceCompatibility = version

    toolchain {
        languageVersion = JavaLanguageVersion.of(versionString)
    }

    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Javadoc> {
    val options = options as StandardJavadocDocletOptions

    options.encoding = "UTF-8"
    options.addBooleanOption("Xdoclint:none,-missing", true)
    options.tags("apiNote:a:API Note:", "implSpec:a:Implementation Requirements:", "implNote:a:Implementation Note:")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit4 test framework
            useJUnit("4.13.2")
        }
    }
}