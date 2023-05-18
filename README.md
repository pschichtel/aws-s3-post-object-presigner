aws-s3-post-object-presigner
============================

This library provides classes to create presigned POST Object requests for AWS S3 (or compatible S3 implementations).

It has been inspired by [this gist](https://gist.github.com/trinopoty/f0272a4a33dcf455b3a7d4a70ed6b715),
but significant changes have been made to improve it.

The library is currently necessary, since the AWS SDK does not provide this itself:
https://github.com/aws/aws-sdk-java-v2/issues/1493
