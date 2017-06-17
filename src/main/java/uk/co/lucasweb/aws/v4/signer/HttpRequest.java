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

/**
 * @author Richard Lucas
 */
public class HttpRequest {

    private final String method;
    private final String path;
    private final String query;

    public HttpRequest(String method, URI uri) {
        this.method = method;
        this.path = uri.getRawPath();
        this.query = uri.getRawQuery();
    }

    public HttpRequest(String method, String pathAndQuery) {
        this.method = method;
        int queryStart = pathAndQuery.indexOf('?');
        if (queryStart >= 0) {
            this.path = pathAndQuery.substring(0, queryStart);
            this.query = pathAndQuery.substring(queryStart + 1);
        } else {
            this.path = pathAndQuery;
            this.query = null;
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }
}
