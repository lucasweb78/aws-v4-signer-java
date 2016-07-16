/*
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  specific language governing permissions and limitations under the License.

  Copyright 2016 the original author or authors.
 */
package uk.co.lucasweb.aws.v4.signer.functional;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides additional functionality missing from the Stream API.
 *
 * @author Richard Lucas
 */
public final class Streams {

    private Streams() {
        // Hide public constructor
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Stream<T> streamopt(Optional<T> optional) {
        return optional.map(Stream::of)
                .orElseGet(Stream::empty);
    }
}
