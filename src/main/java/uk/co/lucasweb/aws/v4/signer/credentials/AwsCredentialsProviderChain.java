/*
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  specific language governing permissions and limitations under the License.

  Copyright 2016 the original author or authors.
 */
package uk.co.lucasweb.aws.v4.signer.credentials;

import org.jetbrains.annotations.Nullable;
import uk.co.lucasweb.aws.v4.signer.SigningException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Richard Lucas
 */
public class AwsCredentialsProviderChain {

    static final String ACCESS_KEY_ENV_VAR = "AWS_ACCESS_KEY";
    static final String SECRET_KEY_ENV_VAR = "AWS_SECRET_KEY";
    static final String ACCESS_KEY_SYSTEM_PROPERTY = "aws.accessKeyId";
    static final String SECRET_KEY_SYSTEM_PROPERTY = "aws.secretKey";

    EnvironmentVarResolver environmentVarResolver;

    private final List<AwsCredentialsProvider> providers;

    public AwsCredentialsProviderChain() {
        this(new EnvironmentVarResolver());
    }

    public AwsCredentialsProviderChain(EnvironmentVarResolver environmentVarResolver) {
        this.environmentVarResolver = environmentVarResolver;
        this.providers = new ArrayList<>();
        this.providers.add(systemPropertiesProvider());
        this.providers.add(environmentProvider());
    }

    public AwsCredentials getCredentials() {
        for (AwsCredentialsProvider provider : providers) {
            AwsCredentials credentials = provider.getCredentials();
            if (credentials != null) {
                return credentials;
            }
        }
        throw new SigningException("no AWS credentials were provided");
    }

    AwsCredentialsProvider environmentProvider() {
        return new AwsCredentialsProvider() {
            @Nullable
            @Override
            public AwsCredentials getCredentials() {
                String accessKey = environmentVarResolver.getenv(ACCESS_KEY_ENV_VAR);
                String secretKey = environmentVarResolver.getenv(SECRET_KEY_ENV_VAR);
                return getAwsCredentials2(accessKey, secretKey);
            }
        };
    }

    AwsCredentialsProvider systemPropertiesProvider() {
        return new AwsCredentialsProvider() {
            @Nullable
            @Override
            public AwsCredentials getCredentials() {
                String accessKey = System.getProperty(ACCESS_KEY_SYSTEM_PROPERTY);
                String secretKey = System.getProperty(SECRET_KEY_SYSTEM_PROPERTY);
                return getAwsCredentials2(accessKey, secretKey);
            }
        };
    }

    @Nullable
    private AwsCredentials getAwsCredentials2(@Nullable String accessKey, @Nullable String secretKey) {
        if (accessKey != null && secretKey != null) {
            return new AwsCredentials(accessKey, secretKey);
        } else {
            return null;
        }
    }

    static class EnvironmentVarResolver {
        String getenv(String name) {
            return System.getenv(name);
        }
    }


}
