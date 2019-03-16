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
import uk.co.lucasweb.aws.v4.signer.hash.Sha256;

import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Richard Lucas
 */
public class Sha256Test {

    private static final String TEST = "PUT\n" +
            "/-/vaults/examplevault\n" +
            "\n" +
            "host:glacier.us-east-1.amazonaws.com\n" +
            "x-amz-date:20120525T002453Z\n" +
            "x-amz-glacier-version:2012-06-01\n" +
            "\n" +
            "host;x-amz-date;x-amz-glacier-version\n" +
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Test
    public void shouldGetSha256() {
        assertThat(Sha256.get(TEST, Charset.forName("UTF-8")))
                .isEqualTo("5f1da1a2d0feb614dd03d71e87928b8e449ac87614479332aced3a701f916743");
    }
}