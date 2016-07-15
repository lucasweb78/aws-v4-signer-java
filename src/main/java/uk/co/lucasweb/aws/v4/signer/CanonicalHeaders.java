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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    Optional<String> getFirstValue(String name) {
        return Optional.ofNullable(internalMap.get(name.toLowerCase()))
                .map(values -> values.get(0));
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
            String lowerCaseName = name.toLowerCase();
            internalMap.put(lowerCaseName, Optional.ofNullable(internalMap.get(lowerCaseName))
                    .map(values -> {
                        values.add(value);
                        return values;
                    })
                    .orElse(newValueListWithValue(value)));
            return this;
        }

        CanonicalHeaders build() {
            String names = internalMap.keySet()
                    .stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.joining(";"));

            StringBuilder canonicalizedHeadersBuilder = new StringBuilder();
            internalMap.entrySet()
                    .forEach(header -> header.getValue().forEach(value -> canonicalizedHeadersBuilder
                            .append(header.getKey().toLowerCase())
                            .append(':')
                            .append(value)
                            .append('\n'))
                    );

            return new CanonicalHeaders(names, canonicalizedHeadersBuilder.toString(), internalMap);
        }

        private List<String> newValueListWithValue(String value) {
            List<String> values = new ArrayList<>();
            values.add(value);
            return values;
        }

    }
}
