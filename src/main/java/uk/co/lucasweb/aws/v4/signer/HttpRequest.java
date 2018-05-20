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

/**
 * @author Richard Lucas
 */
public class HttpRequest {

    private final URI uri;
    private final String method;

    public HttpRequest(String method, URI uri) {
        this.method = method;
        this.uri = uri;
    }

    public HttpRequest(String method, String pathAndQuery) {
        this.method = method;
        try {
            this.uri = new URI("https://hosttodiscard" + pathAndQuery);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getRawPath() {
        return uri.getRawPath();
    }

    public String getPath() {
        return uri.getPath();
    }

    public String getRawQuery() {
        return uri.getRawQuery();
    }

    public String getQuery() {
        return uri.getQuery();
    }
}
