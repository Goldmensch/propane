plugins {
    // Apply the java-library plugin for API and implementation separation.
    id("project.convention.java")
    id("project.convention.maven-central-deploy")
}

dependencies {
    api(libs.org.jspecify)
}

description = "A java framework for library configuration and service registration."
