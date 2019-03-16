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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Richard Lucas
 */
public class CanonicalHeadersTest {

    @Test
    public void shouldBuildCanonicalizedHeaders() throws Exception {
        CanonicalHeaders headers = CanonicalHeaders.builder()
                .add("test", "one")
                .add("test", "two")
                .add("hello", "world")
                .build();

        assertThat(headers.getNames()).isEqualTo("hello;test");
        assertThat(headers.get()).isEqualTo("hello:world\ntest:one,two\n");
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionIfNameIsNull() throws Exception {
        assertThatThrownBy(() -> CanonicalHeaders.builder()
                .add(null, "one"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is null");
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionIfValueIsNull() throws Exception {
        assertThatThrownBy(() -> CanonicalHeaders.builder()
                .add("test", null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("value is null");
    }

    @Test
    public void shouldGetFirstValue() throws Exception {
        CanonicalHeaders headers = CanonicalHeaders.builder()
                .add("test", "one")
                .add("test", "two")
                .add("hello", "world")
                .build();

        assertThat(headers.getFirstValue("test")).isEqualTo("one");
    }

    @Test
    public void shouldReturnEmptyOptionOnGetFirstValue() throws Exception {
        CanonicalHeaders headers = CanonicalHeaders.builder()
                .add("test", "one")
                .add("test", "two")
                .add("hello", "world")
                .build();

        assertThat(headers.getFirstValue("bad")).isNull();
    }
}