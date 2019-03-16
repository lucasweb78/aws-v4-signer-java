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

import uk.co.lucasweb.aws.v4.signer.credentials.AwsCredentials;
import uk.co.lucasweb.aws.v4.signer.credentials.AwsCredentialsProviderChain;
import uk.co.lucasweb.aws.v4.signer.hash.Base16;
import uk.co.lucasweb.aws.v4.signer.hash.Sha256;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Richard Lucas
 */
public class Signer {

    private static final String AUTH_TAG = "AWS4";
    private static final String ALGORITHM = AUTH_TAG + "-HMAC-SHA256";
    private static final Charset UTF_8 = getUtf8();
    private static final String X_AMZ_DATE = "X-Amz-Date";
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final CanonicalRequest request;
    private final AwsCredentials awsCredentials;
    private final String date;
    private final CredentialScope scope;

    private Signer(CanonicalRequest request, AwsCredentials awsCredentials, String date, CredentialScope scope) {
        this.request = request;
        this.awsCredentials = awsCredentials;
        this.date = date;
        this.scope = scope;
    }

    String getCanonicalRequest() {
        return request.get();
    }

    String getStringToSign() {
        String hashedCanonicalRequest = Sha256.get(getCanonicalRequest(), UTF_8);
        return buildStringToSign(date, scope.get(), hashedCanonicalRequest);
    }

    public String getSignature() {
        String signature = buildSignature(awsCredentials.getSecretKey(), scope, getStringToSign());
        return buildAuthHeader(awsCredentials.getAccessKey(), scope.get(), request.getHeaders().getNames(), signature);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String formatDateWithoutTimestamp(String date) {
        return date.substring(0, 8);
    }

    private static String buildStringToSign(String date, String credentialScope, String hashedCanonicalRequest) {
        return ALGORITHM + "\n" + date + "\n" + credentialScope + "\n" + hashedCanonicalRequest;
    }

    private static String buildAuthHeader(String accessKey, String credentialScope, String signedHeaders, String signature) {
        return ALGORITHM + " " + "Credential=" + accessKey + "/" + credentialScope + ", " + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;
    }

    private static byte[] hmacSha256(byte[] key, String value) {
        try {
            String algorithm = HMAC_SHA256;
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec signingKey = new SecretKeySpec(key, algorithm);
            mac.init(signingKey);
            return mac.doFinal(value.getBytes(UTF_8));
        } catch (Exception e) {
            throw new SigningException("Error signing request", e);
        }
    }

    private static String buildSignature(String secretKey, CredentialScope scope, String stringToSign) {
        byte[] kSecret = (AUTH_TAG + secretKey).getBytes(UTF_8);
        byte[] kDate = hmacSha256(kSecret, scope.getDateWithoutTimestamp());
        byte[] kRegion = hmacSha256(kDate, scope.getRegion());
        byte[] kService = hmacSha256(kRegion, scope.getService());
        byte[] kSigning = hmacSha256(kService, CredentialScope.TERMINATION_STRING);
        return Base16.encode(hmacSha256(kSigning, stringToSign)).toLowerCase();
    }

    private static Charset getUtf8() {
        try {
            return Charset.forName("UTF-8");
        } catch (Exception e) {
            throw new SigningException(e);
        }
    }

    public static class Builder {

        private static final String DEFAULT_REGION = "us-east-1";
        private static final String S3 = "s3";
        private static final String GLACIER = "glacier";

        private AwsCredentials awsCredentials;
        private String region = DEFAULT_REGION;
        private List<Header> headersList = new ArrayList<>();

        public Builder awsCredentials(AwsCredentials awsCredentials) {
            this.awsCredentials = awsCredentials;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder header(String name, String value) {
            headersList.add(new Header(name, value));
            return this;
        }

        public Builder header(Header header) {
            headersList.add(header);
            return this;
        }

        public Builder headers(Header... headers) {
            Collections.addAll(headersList, headers);
            return this;
        }

        public Signer build(HttpRequest request, String service, String contentSha256) {
            CanonicalHeaders canonicalHeaders = getCanonicalHeaders();
            String date = getDate(canonicalHeaders);
            String dateWithoutTimestamp = formatDateWithoutTimestamp(date);
            AwsCredentials awsCredentials = getAwsCredentials();
            CanonicalRequest canonicalRequest = new CanonicalRequest(service, request, canonicalHeaders, contentSha256);
            CredentialScope scope = new CredentialScope(dateWithoutTimestamp, service, region);
            return new Signer(canonicalRequest, awsCredentials, date, scope);
        }

        private String getDate(CanonicalHeaders canonicalHeaders) {
            String value = canonicalHeaders.getFirstValue(X_AMZ_DATE);
            if (value == null) {
                throw new SigningException("headers missing '" + X_AMZ_DATE + "' header");
            }
            return value;
        }

        public Signer buildS3(HttpRequest request, String contentSha256) {
            return build(request, S3, contentSha256);
        }

        public Signer buildGlacier(HttpRequest request, String contentSha256) {
            return build(request, GLACIER, contentSha256);
        }

        private AwsCredentials getAwsCredentials() {
            return awsCredentials == null ? new AwsCredentialsProviderChain().getCredentials() : awsCredentials;
        }

        private CanonicalHeaders getCanonicalHeaders() {
            CanonicalHeaders.Builder builder = CanonicalHeaders.builder();
            for (Header header : headersList) {
                builder.add(header.getName(), header.getValue());
            }
            return builder.build();
        }

    }
}
