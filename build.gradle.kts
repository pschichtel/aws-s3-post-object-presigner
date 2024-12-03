plugins {
    signing
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin")
    `java-library`
}

group = "tel.schich"
version = "0.1.2-SNAPSHOT"
description = "Provides a S3 PostObject request signer"

repositories {
    mavenCentral()
}

dependencies {
    api("software.amazon.awssdk:s3:2.29.24")
    implementation("com.google.code.gson:gson:2.11.0")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("central") {
            pom {
                from(components["java"])
                name.set(rootProject.name)
                description.set(rootProject.description)
                url.set("https://github.com/pschichtel/aws-s3-post-signer")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("pschichtel")
                        name.set("Phillip Schichtel")
                        email.set("phillip@schich.tel")
                        roles.add("Library Author")
                    }
                }
                scm {
                    url.set("https://github.com/pschichtel/aws-s3-post-signer")
                    connection.set("scm:git:https://github.com/pschichtel/aws-s3-post-signer")
                    developerConnection.set("scm:git:git@github.com:pschichtel/aws-s3-post-signer")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

nexusPublishing {
    this.repositories {
        sonatype()
    }
}