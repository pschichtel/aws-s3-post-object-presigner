rootProject.name = "aws-s3-post-object-presigner"

pluginManagement {

    val nexusPublishingVersion: String by settings

    plugins {
        id("io.github.gradle-nexus.publish-plugin") version(nexusPublishingVersion)
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.9.0")
}