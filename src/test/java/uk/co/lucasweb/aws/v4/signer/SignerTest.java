/*
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  specific language governing permissions and limitations under the License.

  Copyright 2016 the original author or authors.
 */
package uk.co.lucasweb.aws.v4.signer;

import org.junit.Test;
import uk.co.lucasweb.aws.v4.signer.credentials.AwsCredentials;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Richard Lucas
 */
public class SignerTest {

    private static final String ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
    private static final String SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";



    @Test
    public void shouldSignRequest() throws Exception {
        // the values used in this test are from the example http://docs.aws.amazon.com/amazonglacier/latest/dev/amazon-glacier-signing-requests.html
        String hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        HttpRequest request = new HttpRequest("PUT", new URI("https://glacier.us-east-1.amazonaws.com/-/vaults/examplevault"));

        String signature = Signer.builder()
                .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
                .header("Host", "glacier.us-east-1.amazonaws.com")
                .header("x-amz-date", "20120525T002453Z")
                .header("x-amz-glacier-version", "2012-06-01")
                .buildAuthHeader(request, "glacier", hash)
                .getSignature();

        String expectedSignature = "AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20120525/us-east-1/glacier/aws4_request, " +
                "SignedHeaders=host;x-amz-date;x-amz-glacier-version, Signature=3ce5b2f2fffac9262b4da9256f8d086b4aaf42eba5f111c21681a65a127b7c2a";

        assertThat(signature).isEqualTo(expectedSignature);
    }

    @Test
    public void shouldSignRequestWithQueryParam() throws Exception {
        String hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        HttpRequest request = new HttpRequest("GET", new URI("https://examplebucket.s3.amazonaws.com?max-keys=2&prefix=J"));

        String signature = Signer.builder()
                .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
                .header("Host", "examplebucket.s3.amazonaws.com")
                .header("x-amz-date", "20130524T000000Z")
                .header("x-amz-content-sha256", hash)
                .buildAuthHeader(request, "s3", hash)
                .getSignature();

        String expectedSignature = "AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request, " +
                "SignedHeaders=host;x-amz-content-sha256;x-amz-date, Signature=34b48302e7b5fa45bde8084f4b7868a86f0a534bc59db6670ed5711ef69dc6f7";

        assertThat(signature).isEqualTo(expectedSignature);
    }

    @Test
    public void shouldSignStreamingRequest() throws Exception {
        // see http://docs.aws.amazon.com/amazonglacier/latest/dev/amazon-glacier-signing-requests.html
        String contentHash = "79da47e784b181ae04e5b5119fcc953d944acad5e0583fa0899d554a79eb77eb";
        String treeHash = "05c734c3f16b23358bb49c959d1420edac9f28ee844bf9b0580754c0f540acd8";
        URI uri = new URI("https://glacier.us-east-1.amazonaws.com/-/vaults/dev2/multipart-uploads/j3eqysOZoNF3UiEoN3k_b6bdRGGdzgEfsLoUyZhMIwKRMuDLEYRw2nlCh8QXQ_dzqQMxrgFtmZjatxbFIZ9HpnIUi93B");

        HttpRequest request = new HttpRequest("PUT", uri);

        String signature = Signer.builder()
                .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
                .header("Accept", "application/json")
                .header("Content-Length", "1049350")
                .header("Content-Range", "bytes 0-1049349/*")
                .header("Content-Type", "binary/octet-stream")
                .header("Host", "glacier.us-east-1.amazonaws.com")
                .header("user-agent", "aws-sdk-java/1.9.26 Mac_OS_X/10.10.3 Java_HotSpot(TM)_64-Bit_Server_VM/25.0-b70/1.8.0")
                .header("x-amz-content-sha256", contentHash)
                .header("X-Amz-Date", "20150424T222200Z")
                .header("x-amz-glacier-version", "2012-06-01")
                .header("x-amz-sha256-tree-hash", treeHash)
                .header("X-Amz-Target", "Glacier.UploadMultipartPart")
                .buildAuthHeader(request,"glacier", contentHash)
                .getSignature();

        String expectedSignature = "AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20150424/us-east-1/glacier/aws4_request, " +
                "SignedHeaders=accept;content-length;content-range;content-type;host;user-agent;x-amz-content-sha256;x-amz-date;x-amz-glacier-version;x-amz-sha256-tree-hash;x-amz-target, " +
                "Signature=3ee337a197d3b15e719fd21acf378ef2d62f73159ff3c47fc0204e27e5ee9fb1";

        assertThat(signature).isEqualTo(expectedSignature);
    }

    @Test
    public void shouldSignS3AuthHeader() throws URISyntaxException {
        // the values used in this test are from the example https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-header-based-auth.html
        String contentSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        HttpRequest request = new HttpRequest("GET", new URI("https://examplebucket.s3.amazonaws.com/test.txt"));
        String signature = Signer.builder()
                .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
                .header("Host", "examplebucket.s3.amazonaws.com")
                .header("range", "bytes=0-9")
                .header("x-amz-date", "20130524T000000Z")
                .header("x-amz-content-sha256", contentSha256)
                .buildAuthHeader(request, "s3", contentSha256)
                .getSignature();

        String expectedSignature = "AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request, " +
                "SignedHeaders=host;range;x-amz-content-sha256;x-amz-date, Signature=f0e8bdb87c964420e857bd35b5d6ed310bd44f0170aba48dd91039c6036bdb41";

        assertThat(signature).isEqualTo(expectedSignature);
    }

    @Test
    public void shouldSignS3QueryString() throws Exception {
        // the values used in this test are from the example https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-query-string-auth.html
        HttpRequest request = new HttpRequest("GET", new URI("https://examplebucket.s3.amazonaws.com/test.txt"));
        int expires = 86400;

        String signature = Signer.builder()
                .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
                .header("Host", "examplebucket.s3.amazonaws.com")
                .buildQueryString(request, "s3", "UNSIGNED-PAYLOAD", "20130524T000000Z", expires)
                .getSignature();

        String expectedSignature = "X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request&X-Amz-Date=20130524T000000Z&X-Amz-Expires=86400&X-Amz-SignedHeaders=host&X-Amz-Signature=aeeed9bbccd4d02ee5c0109b86d86835f995330da4c265957d157751f604d404";

        assertThat(signature).isEqualTo(expectedSignature);
    }
}