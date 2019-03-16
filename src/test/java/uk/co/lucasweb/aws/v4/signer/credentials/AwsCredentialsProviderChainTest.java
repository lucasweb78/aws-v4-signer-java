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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.co.lucasweb.aws.v4.signer.SigningException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Richard Lucas
 */
public class AwsCredentialsProviderChainTest {

    private AwsCredentialsProviderChain chain;

    @Before
    public void setUp() throws Exception {
        chain = new AwsCredentialsProviderChain();
        chain.environmentVarResolver = mock(AwsCredentialsProviderChain.EnvironmentVarResolver.class);
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(AwsCredentialsProviderChain.ACCESS_KEY_SYSTEM_PROPERTY);
        System.clearProperty(AwsCredentialsProviderChain.SECRET_KEY_SYSTEM_PROPERTY);
    }

    @Test
    public void shouldGetCredentialsUsingSystemProperties() throws Exception {
        System.setProperty(AwsCredentialsProviderChain.ACCESS_KEY_SYSTEM_PROPERTY, "access");
        System.setProperty(AwsCredentialsProviderChain.SECRET_KEY_SYSTEM_PROPERTY, "secret");
        assertThat(chain.systemPropertiesProvider().getCredentials())
                .isEqualToComparingFieldByField(new AwsCredentials("access", "secret"));
    }

    @Test
    public void shouldGetCredentialsUsingEnvironmentProperties() throws Exception {
        doReturn("env_access")
                .when(chain.environmentVarResolver)
                .getenv(eq(AwsCredentialsProviderChain.ACCESS_KEY_ENV_VAR));

        doReturn("env_secret")
                .when(chain.environmentVarResolver)
                .getenv(eq(AwsCredentialsProviderChain.SECRET_KEY_ENV_VAR));

        assertThat(chain.environmentProvider().getCredentials())
                .isEqualToComparingFieldByField(new AwsCredentials("env_access", "env_secret"));
    }

    @Test
    public void shouldThrowSigningExceptionInNoCredentials() throws Exception {
        assertThatThrownBy(() -> chain.getCredentials())
                .isExactlyInstanceOf(SigningException.class)
                .hasMessage("no AWS credentials were provided");
    }
}