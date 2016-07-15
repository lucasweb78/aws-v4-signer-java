#aws-v4-signer-java

aws-v4-signer-java is a lightweight, zero-dependency implementation of the AWS V4 signing algorithm required by many of the AWS services. 

Requires Java 8+.

## Setup

Add the latest aws-v4-signer-java Maven dependency to your project

```xml
<dependency>
  <groupId>uk.co.lucasweb</groupId>
  <artifactId>aws-v4-signer-java</artifactId>
  <version>1.1</version>
</dependency>
```

## Usage

### S3

```java
String contentSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
HttpRequest request = new HttpRequest("GET", new URI("https://examplebucket.s3.amazonaws.com?max-keys=2&prefix=J"));
String signature = Signer.builder()
        .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
        .header("Host", "examplebucket.s3.amazonaws.com")
        .header("x-amz-date", "20130524T000000Z")
        .header("x-amz-content-sha256", contentSha256)
        .buildS3(request, contentSha256)
        .getSignature();
```

### Glacier

```java
String contentSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
HttpRequest request = new HttpRequest("PUT", new URI("https://glacier.us-east-1.amazonaws.com/-/vaults/examplevault"));
String signature = Signer.builder()
        .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
        .header("Host", "glacier.us-east-1.amazonaws.com")
        .header("x-amz-date", "20120525T002453Z")
        .header("x-amz-glacier-version", "2012-06-01")
        .buildGlacier(request, contentSha256)
        .getSignature();
```