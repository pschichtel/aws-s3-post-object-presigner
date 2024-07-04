aws-s3-post-object-presigner [![Maven Central](https://img.shields.io/maven-central/v/tel.schich/aws-s3-post-object-presigner.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22tel.schich%22%20AND%20a:%22aws-s3-post-object-presigner%22)
============================

This library provides classes to create presigned POST Object requests for AWS S3 (or compatible S3 implementations).

It has been inspired by [this gist](https://gist.github.com/trinopoty/f0272a4a33dcf455b3a7d4a70ed6b715),
but significant changes have been made to improve it.

The library is currently necessary, since the AWS SDK does not provide this itself:
https://github.com/aws/aws-sdk-java-v2/issues/1493
