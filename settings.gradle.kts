rootProject.name = "aws-s3-post-signer"

pluginManagement {

    val nexusPublishingVersion: String by settings

    plugins {
        id("io.github.gradle-nexus.publish-plugin") version(nexusPublishingVersion)
    }
}
