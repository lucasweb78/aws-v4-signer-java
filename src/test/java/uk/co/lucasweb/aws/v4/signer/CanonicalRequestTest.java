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

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Richard Lucas
 */
public class CanonicalRequestTest {

    private static final String EXPECTED_GLACIER = "PUT\n" +
            "/-/vaults/examplevault\n" +
            "\n" +
            "host:glacier.us-east-1.amazonaws.com\n" +
            "x-amz-date:20120525T002453Z\n" +
            "x-amz-glacier-version:2012-06-01\n" +
            "\n" +
            "host;x-amz-date;x-amz-glacier-version\n" +
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private static final String EXPECTED_S3 = "PUT\n" +
            "/my-object//example//photo.user\n" +
            "\n" +
            "host:s3.us-east-1.amazonaws.com\n" +
            "x-amz-date:20120525T002453Z\n" +
            "\n" +
            "host;x-amz-date\n" +
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Test
    public void shouldGetGlacierCanonicalRequest() throws Exception {
        HttpRequest request = new HttpRequest("PUT", new URI("https://glacier.us-east-1.amazonaws.com/-/vaults///./examplevault"));
        CanonicalHeaders headers = CanonicalHeaders.builder()
                .add("Host", "glacier.us-east-1.amazonaws.com")
                .add("x-amz-date", "20120525T002453Z")
                .add("x-amz-glacier-version", "2012-06-01")
                .build();
        String hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        assertThat(new CanonicalRequest("glacier", request, headers, hash).get())
                .isEqualTo(EXPECTED_GLACIER);
    }

    @Test
    public void shouldGetS3CanonicalRequest() throws Exception {
        HttpRequest request = new HttpRequest("PUT", new URI("https://s3.us-east-1.amazonaws.com/my-object//example//photo.user"));
        CanonicalHeaders headers = CanonicalHeaders.builder()
                .add("Host", "s3.us-east-1.amazonaws.com")
                .add("x-amz-date", "20120525T002453Z")
                .build();
        String hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        assertThat(new CanonicalRequest("s3", request, headers, hash).get())
                .isEqualTo(EXPECTED_S3);
    }
}