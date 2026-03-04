plugins {
    // Apply the java-library plugin for API and implementation separation.
    id("project.convention.java")
    id("project.convention.maven-central-deploy")
}

dependencies {
    api(libs.org.jspecify)

    api("com.palantir.javapoet:javapoet:0.12.0")

    testAnnotationProcessor(project(":propane"))
}

description = "A java framework for library configuration and service registration."
