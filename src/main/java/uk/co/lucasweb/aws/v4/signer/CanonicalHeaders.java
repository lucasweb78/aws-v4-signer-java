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

import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Canonical Headers.
 * <p>
 * See http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html for more information
 * </p>
 *
 * @author Richard Lucas
 */
class CanonicalHeaders {

    private final String names;
    private final String canonicalizedHeaders;
    private final TreeMap<String, List<String>> internalMap;

    private CanonicalHeaders(String names, String canonicalizedHeaders, TreeMap<String, List<String>> internalMap) {
        this.names = names;
        this.canonicalizedHeaders = canonicalizedHeaders;
        this.internalMap = internalMap;
    }

    String get() {
        return canonicalizedHeaders;
    }

    String getNames() {
        return names;
    }

    @Nullable
    String getFirstValue(String name) {
        List<String> values = internalMap.get(name.toLowerCase());
        return values == null ? null : values.get(0);
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {

        private final TreeMap<String, List<String>> internalMap = new TreeMap<>();

        Builder add(String name, String value) {

            if (name == null) {
                throw new IllegalArgumentException("name is null");
            }

            if (value == null) {
                throw new IllegalArgumentException("value is null");
            }
            String key = name.toLowerCase();
            List<String> values = newValueListWithValue(value, key);
            internalMap.put(key, values);
            return this;
        }

        private List<String> newValueListWithValue(String value, String lowerCaseName) {
            List<String> values = internalMap.get(lowerCaseName);
            if (values == null) {
                return newValueListWithValue(value);
            } else {
                values.add(value);
                return values;
            }
        }

        CanonicalHeaders build() {
            String names = collect(internalMap.keySet());

            StringBuilder canonicalizedHeadersBuilder = new StringBuilder();
            for (Map.Entry<String, List<String>> header : internalMap.entrySet()) {
                canonicalizedHeadersBuilder
                        .append(header.getKey().toLowerCase())
                        .append(':')
                        .append(collectNormalized(header.getValue()))
                        .append('\n');
            }

            return new CanonicalHeaders(names, canonicalizedHeadersBuilder.toString(), internalMap);
        }

        private String collect(Set<String> names) {
            StringBuilder stringBuilder = new StringBuilder(names.size());
            for (String key : names) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(";");
                }
                stringBuilder.append(key.toLowerCase());
            }
            return stringBuilder.toString();
        }

        private String collectNormalized(List<String> values) {
            StringBuilder stringBuilder = new StringBuilder(values.size());
            for (String value : values) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(",");
                }
                stringBuilder.append(normalizeHeaderValue(value));
            }
            return stringBuilder.toString();
        }

        private List<String> newValueListWithValue(String value) {
            List<String> values = new ArrayList<>();
            values.add(value);
            return values;
        }

        private static String normalizeHeaderValue(String value) {
            /*
             * Strangely, the AWS test suite expects us to handle lines in
             * multi-line values as individual values, even though this is not
             * mentioned in the specs.
             */
            String[] strings = value.split("\n");
            StringBuilder stringBuilder = new StringBuilder(strings.length);
            for (String string : strings) {
                // Remove spaces on the edges of the string
                String trimmed = string.trim();

                // Remove duplicate spaces inside the string
                String sanitised = trimmed.replaceAll(" +", " ");
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(",");
                }
                stringBuilder.append(sanitised);
            }
            return stringBuilder.toString();
        }

    }
}
