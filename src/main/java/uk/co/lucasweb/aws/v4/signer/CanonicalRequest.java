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

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import uk.co.lucasweb.aws.v4.signer.encoding.URLEncoding;

/**
 * @author Richard Lucas
 */
class CanonicalRequest {

    private static final String S3_SERVICE = "s3";
    private static final char QUERY_PARAMETER_SEPARATOR = '&';
    private static final char QUERY_PARAMETER_VALUE_SEPARATOR = '=';

    private final String service;
    private final HttpRequest httpRequest;
    private final CanonicalHeaders headers;
    private final String contentSha256;

    CanonicalRequest(String service, HttpRequest httpRequest, CanonicalHeaders headers, String contentSha256) {
        this.service = service;
        this.httpRequest = httpRequest;
        this.headers = headers;
        this.contentSha256 = contentSha256;
    }

    String get() {
        return httpRequest.getMethod() +
                "\n" + normalizePath(httpRequest.getPath()) +
                "\n" + normalizeQuery(httpRequest.getQuery()) +
                "\n" + headers.get() +
                "\n" + headers.getNames() +
                "\n" + contentSha256;
    }

    CanonicalHeaders getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return get();
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        // Encode characters as mandated by AWS
        String encoded = URLEncoding.encodePath(path);
        if (S3_SERVICE.equals(service)) {
            /*
             * S3 requests should not be normalized.
             * See http://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-header-based-auth.html#canonical-request
             */
            return encoded;
        }
        // Normalize paths such as "/foo/..", "/./", "/foo//bar/", ...
        try {
            // Use "http://" as a prefix, so that paths such as "//" are deemed syntactically correct
            return new URI("http://" + encoded).normalize().getRawPath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                    "The encoded path '" + path + "' was deemed syntactically incorrect;"
                    + " there is probably an internal issue with the encoding algorithm"
            );
        }
    }

    private static String normalizeQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return "";
        }

        List<Parameter> parameters = extractQueryParameters(rawQuery);

        /*
         * Sort query parameters. Simply sort lexicographically by character
         * code, which is equivalent to comparing code points (as mandated by
         * AWS)
         */
        parameters.sort(new Comparator<Parameter>() {
            @Override
            public int compare(Parameter l, Parameter r) {
                return l.name.compareTo(r.name);
            }
        });

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Parameter parameter : parameters) {
            if (first) {
                first = false;
            } else {
                builder.append(QUERY_PARAMETER_SEPARATOR);
            }
            String name = parameter.name;
            String value = parameter.value;
            if (value == null) {
                // No value => use an empty string as per the spec
                value = "";
            }
            builder.append(URLEncoding.encodeQueryComponent(name))
                    .append(QUERY_PARAMETER_VALUE_SEPARATOR)
                    .append(URLEncoding.encodeQueryComponent(value));
        }

        return builder.toString();
    }

    /**
     * Extract parameters from a query string, preserving encoding.
     * <p>
     * We can't use Apache HTTP Client's URLEncodedUtils.parse, mainly because
     * we don't want to decode names/values.
     *
     * @param rawQuery
     *            the query to parse
     * @return The list of parameters, in the order they were found.
     */
    private static List<Parameter> extractQueryParameters(String rawQuery) {
        List<Parameter> results = new ArrayList<>();
        int endIndex = rawQuery.length() - 1;
        int index = 0;
        while (0 <= index && index <= endIndex) {
            /*
             * Ideally we should first look for '&', then look for '=' before
             * the '&', but obviously that's not how AWS understand query
             * parsing; see the test "post-vanilla-query-nonunreserved" in the
             * test suite. A string such as "?foo&bar=qux" will be understood as
             * one parameter with name "foo&bar" and value "qux". Don't ask me
             * why.
             */
            String name;
            String value;
            int nameValueSeparatorIndex = rawQuery.indexOf(QUERY_PARAMETER_VALUE_SEPARATOR, index);
            if (nameValueSeparatorIndex < 0) {
                // No value
                name = rawQuery.substring(index);
                value = null;

                index = endIndex + 1;
            } else {
                int parameterSeparatorIndex = rawQuery.indexOf(QUERY_PARAMETER_SEPARATOR, nameValueSeparatorIndex);
                if (parameterSeparatorIndex < 0) {
                    parameterSeparatorIndex = endIndex + 1;
                }
                name = rawQuery.substring(index, nameValueSeparatorIndex);
                value = rawQuery.substring(nameValueSeparatorIndex + 1, parameterSeparatorIndex);

                index = parameterSeparatorIndex + 1;
            }

            results.add(new Parameter(name, value));
        }
        return results;
    }

    private static final class Parameter {
        private final String name;
        private final String value;

        public Parameter(String name, String value) {
            super();
            this.name = name;
            this.value = value;
        }
    }
}
