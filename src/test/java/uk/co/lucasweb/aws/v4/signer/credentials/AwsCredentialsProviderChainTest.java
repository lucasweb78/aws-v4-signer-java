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
                .usingFieldByFieldValueComparator()
                .hasValue(new AwsCredentials("access", "secret"));
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
                .usingFieldByFieldValueComparator()
                .hasValue(new AwsCredentials("env_access", "env_secret"));
    }

    @Test
    public void shouldThrowSigningExceptionInNoCredentials() throws Exception {
        assertThatThrownBy(() -> chain.getCredentials())
                .isExactlyInstanceOf(SigningException.class)
                .hasMessage("no AWS credentials were provided");
    }
}