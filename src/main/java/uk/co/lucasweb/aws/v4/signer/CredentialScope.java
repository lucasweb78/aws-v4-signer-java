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

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yoann Rodiere
 */
class CredentialScope {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    static final String TERMINATION_STRING = "aws4_request";

    private final String dateWithoutTimestamp;
    private final String service;
    private final String region;

    public CredentialScope(String dateWithoutTimestamp, String service, String region) {
        super();
        this.dateWithoutTimestamp = dateWithoutTimestamp;
        this.service = service;
        this.region = region;
    }

    String getDateWithoutTimestamp() {
        return dateWithoutTimestamp;
    }

    String getService() {
        return service;
    }

    String getRegion() {
        return region;
    }

    String get() {
        String credentialScope = dateWithoutTimestamp + "/" + region + "/" + service + "/" + TERMINATION_STRING;
        LOGGER.debug("CredentialScope = '{}'", credentialScope);
        return credentialScope;
    }

}
