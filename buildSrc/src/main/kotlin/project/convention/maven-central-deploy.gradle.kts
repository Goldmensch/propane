package project.convention

import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.jreleaser.model.Active

plugins {
    `maven-publish`
    id("org.jreleaser")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set(rootProject.name)
                description.set("test")
                url.set("https://github.com/Goldmensch/propane")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("http://choosealicense.com/licenses/mit/")
                    }
                }

                developers {
                    developer {
                        name.set("Goldmensch")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Goldmensch/propane")
                    developerConnection.set("scm:git:ssh://github.com/Goldmensch/propane")
                    url.set("https://github.com/Goldmensch/propane")
                }
            }
        }
    }

    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    project {
        copyright = "Goldmensch"
    }


    signing {
        active = Active.ALWAYS
        armored = true
    }

    deploy {
        maven {
            mavenCentral {
                create("release") {
                    active = Active.RELEASE
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                    setStage("UPLOAD")
                }
            }

            nexus2 {
                create("snapshot") {
                    active = Active.SNAPSHOT
                    url = "https://central.sonatype.com/api/v1/publisher"
                    snapshotUrl = "https://central.sonatype.com/repository/maven-snapshots/"
                    applyMavenCentralRules = true
                    snapshotSupported = true
                    closeRepository = true
                    releaseRepository = true
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}

tasks.jreleaserDeploy {
    dependsOn(tasks.publish)
}