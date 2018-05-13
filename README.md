#aws-v4-signer-java

aws-v4-signer-java is a lightweight, zero-dependency implementation of the AWS V4 signing algorithm required by many of the AWS services. 

Requires Java 8+.

## Setup

Add the latest aws-v4-signer-java Maven dependency to your project

```xml
<dependency>
  <groupId>uk.co.lucasweb</groupId>
  <artifactId>aws-v4-signer-java</artifactId>
  <version>1.3</version>
</dependency>
```

## Usage


### S3

```java
public class Example {
    // Example from https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-query-string-auth.html
    HttpRequest request = new HttpRequest("GET", new URI("https://examplebucket.s3.amazonaws.com?max-keys=2&prefix=J"));
    String queryString = Signer.builder()
            .awsCredentials(new AwsCredentials("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"))
            .header("Host", "examplebucket.s3.amazonaws.com")
            .buildQueryString(request, "s3", "UNSIGNED-PAYLOAD", "20130524T000000Z", 86400)
            .getSignature();
    
    // X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request&X-Amz-Date=20130524T000000Z&X-Amz-Expires=86400&X-Amz-SignedHeaders=host&X-Amz-Signature=aeeed9bbccd4d02ee5c0109b86d86835f995330da4c265957d157751f604d404
}
```

```java
public class Example {
    // Example from https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-header-based-auth.html
    String contentSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    HttpRequest request = new HttpRequest("GET", new URI("https://examplebucket.s3.amazonaws.com/test.txt"));
    String authHeader = Signer.builder()
            .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
            .header("Host", "examplebucket.s3.amazonaws.com")
            .header("range", "bytes=0-9")
            .header("x-amz-date", "20130524T000000Z")
            .header("x-amz-content-sha256", contentSha256)
            .buildAuthHeader(request, "s3", contentSha256)
            .getSignature();
    
    // AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request, SignedHeaders=host;range;x-amz-content-sha256;x-amz-date, Signature=f0e8bdb87c964420e857bd35b5d6ed310bd44f0170aba48dd91039c6036bdb41
}
```

### Glacier

```java
public class Example {
    // Example from http://docs.aws.amazon.com/amazonglacier/latest/dev/amazon-glacier-signing-requests.html
    String contentSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    HttpRequest request = new HttpRequest("PUT", new URI("https://glacier.us-east-1.amazonaws.com/-/vaults/examplevault"));
    String authHeader = Signer.builder()
            .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
            .header("Host", "glacier.us-east-1.amazonaws.com")
            .header("x-amz-date", "20120525T002453Z")
            .header("x-amz-glacier-version", "2012-06-01")
            .buildAuthHeader(request, "glacier", contentSha256)
            .getSignature();
    
    // AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20120525/us-east-1/glacier/aws4_request, SignedHeaders=host;x-amz-date;x-amz-glacier-version, Signature=3ce5b2f2fffac9262b4da9256f8d086b4aaf42eba5f111c21681a65a127b7c2a
}
```

### MediaStore

```java
public class Example {
    String contentSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    HttpRequest request = new HttpRequest("GET", new URI("https://examplestore.data.mediastore.us-east-1.amazonaws.com/example/file"));
    String queryString = Signer.builder()
        .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
        .header("Host", "examplestore.data.mediastore.us-east-1.amazonaws.com")
        .buildQueryString(request, "mediastore", emptySha256, "20130524T000000Z", 86400)
        .getSignature();
    
    // X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/mediastore/aws4_request&X-Amz-Date=20130524T000000Z&X-Amz-Expires=86400&X-Amz-SignedHeaders=host&X-Amz-Signature=8d2f33a59179a8cdf1379468245bfcdf7302a8ce514734237c100cd701a71dd6
}
```