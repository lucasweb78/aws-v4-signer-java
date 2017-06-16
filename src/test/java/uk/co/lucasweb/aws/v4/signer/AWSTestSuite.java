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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.co.lucasweb.aws.v4.signer.credentials.AwsCredentials;
import uk.co.lucasweb.aws.v4.signer.hash.Sha256;

/**
 * @author Yoann Rodiere
 */
@RunWith(Parameterized.class)
public class AWSTestSuite {

    @Parameters(name = "{0}")
    public static Object[] data() throws IOException, URISyntaxException {
        // Obtains the folder of /src/test/resources
        URL url = ClassLoader.getSystemResource("aws-sig-v4-test-suite/tests/");
        Path testPath = Paths.get(url.toURI());
        try (Stream<Path> stream = Files.walk(testPath)) {
            return stream.filter(AWSTestSuite::isTestDirectory).map(AWSTestSuite::parseTestData).toArray();
        }
    }

    private static final String ACCESS_KEY = "AKIDEXAMPLE";
    private static final String SECRET_KEY = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
    private static final String REGION = "us-east-1";
    private static final String SERVICE = "service";

    private final TestData testData;
    private final Signer signer;

    public AWSTestSuite(TestData testData) {
        super();
        this.testData = testData;

        Assume.assumeFalse(
                "This test is probably buggy: it expects us to translate '/?p aram1=val ue1' to '/?p=' without any reason.",
                "post-vanilla-query-space".equals(testData.name));

        TestAWSRequestToSign request = testData.request;

        Signer.Builder builder = Signer.builder().awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
                .region(REGION);
        for (Header header : testData.request.headers) {
            builder.header(header);
        }

        HttpRequest httpRequest = new HttpRequest(request.method, request.pathAndQuery);
        this.signer = builder.build(httpRequest, SERVICE, request.contentHash);
    }

    @Test
    public void canonicalRequest() {
        Assert.assertEquals("Invalid canonical request", testData.expectedCanonicalRequest, signer.getCanonicalRequest());
    }

    @Test
    public void stringToSign() {
        Assert.assertEquals("Invalid string to sign", testData.expectedStringToSign, signer.getStringToSign());
    }

    @Test
    public void signature() {
        Assert.assertEquals("Invalid signature", testData.expectedSignature, signer.getSignature());
    }

    private static boolean isTestDirectory(Path path) {
        String name = path.getFileName().toString();
        Path requestFile = path.resolve(name + ".req");
        return Files.exists(requestFile);
    }

    private static TestData parseTestData(Path directory) {
        String name = directory.getFileName().toString();
        try {
            TestAWSRequestToSign request = parseRequest(directory.resolve(Paths.get(name + ".req")));
            String expectedCanonicalRequest = readString(directory.resolve(Paths.get(name + ".creq")));
            String expectedStringToSign = readString(directory.resolve(Paths.get(name + ".sts")));
            String expectedSignature = readString(directory.resolve(Paths.get(name + ".authz")));
            return new TestData(name, request, expectedCanonicalRequest, expectedStringToSign, expectedSignature);
        } catch (RuntimeException | IOException | URISyntaxException e) {
            throw new IllegalStateException("Could not read test data at '" + directory + "'", e);
        }
    }

    private static TestAWSRequestToSign parseRequest(Path requestFile) throws IOException, URISyntaxException {
        List<String> lines = Files.readAllLines(requestFile);

        Iterator<String> it = lines.iterator();

        String requestLine = it.next();
        requestLine = requestLine.replaceAll(" HTTP/1.1$", "");

        String[] requestLineParts = splitOnFirst(requestLine, ' ');
        String method = requestLineParts[0];
        // Remove the zero-width non-breaking spaces (codepoint 65279) in some
        // files...
        method = method.replaceAll("\\p{C}", "");
        String pathAndQuery = requestLineParts[1];

        List<Header> headers = parseHeaders(it);

        String contentHash = Sha256.get(parseContent(it), StandardCharsets.UTF_8);

        return new TestAWSRequestToSign(method, pathAndQuery, headers, contentHash);
    }

    private static List<Header> parseHeaders(Iterator<String> it) {
        List<Header> headers = new ArrayList<>();
        while (it.hasNext()) {
            String line = it.next();
            if (line.isEmpty()) {
                break;
            }
            if (line.startsWith(" ")) {
                // Multi-line value
                int lastIndex = headers.size() - 1;
                Header previousHeader = headers.get(lastIndex);
                Header newHeader = new Header(previousHeader.getName(), previousHeader.getValue() + "\n" + line);
                headers.set(lastIndex, newHeader);
            } else {
                String[] headerLineParts = splitOnFirst(line, ':');
                String headerName = headerLineParts[0].toLowerCase(Locale.ROOT);
                String headerValue = headerLineParts[1];
                headers.add(new Header(headerName, headerValue));
            }
        }
        return headers;
    }

    private static String parseContent(Iterator<String> it) {
        StringBuilder content = new StringBuilder();
        boolean firstLine = true;
        while (it.hasNext()) {
            String line = it.next();
            if (firstLine) {
                firstLine = false;
            } else {
                content.append("\n");
            }
            content.append(line);
        }
        return content.toString();
    }

    private static String[] splitOnFirst(String line, char separator) {
        int firstSeparator = line.indexOf(separator);
        return new String[] { line.substring(0, firstSeparator), line.substring(firstSeparator + 1, line.length()) };
    }

    private static String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static final class TestData {

        private final String name;

        private final TestAWSRequestToSign request;

        private final String expectedCanonicalRequest;

        private final String expectedStringToSign;

        private final String expectedSignature;

        private TestData(String name, TestAWSRequestToSign request,
                String expectedCanonicalRequest, String expectedStringToSign, String expectedSignature) {
            this.name = name;
            this.request = request;
            this.expectedCanonicalRequest = expectedCanonicalRequest;
            this.expectedStringToSign = expectedStringToSign;
            this.expectedSignature = expectedSignature;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final class TestAWSRequestToSign {

        private final String method;
        private final String pathAndQuery;
        private final List<Header> headers;
        private final String contentHash;

        public TestAWSRequestToSign(String method, String pathAndQuery, List<Header> headers, String contentHash) {
            super();
            this.method = method;
            this.pathAndQuery = pathAndQuery;
            this.headers = headers;
            this.contentHash = contentHash;
        }
    }

}
