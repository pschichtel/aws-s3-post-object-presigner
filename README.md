aws-s3-post-signer
==================

This library provides classes to create presigned POST Object requests for AWS S3 (or compatible S3 implementations).

it simply bundles the code provided [here](https://gist.github.com/trinopoty/f0272a4a33dcf455b3a7d4a70ed6b715)
into a library that is published to maven central.

This is currently necessary, since the AWS SDK does not provide this itself:
https://github.com/aws/aws-sdk-java-v2/issues/1493
