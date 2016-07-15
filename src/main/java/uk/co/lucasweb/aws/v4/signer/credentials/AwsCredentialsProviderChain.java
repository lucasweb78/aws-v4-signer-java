package uk.co.lucasweb.aws.v4.signer.credentials;

import uk.co.lucasweb.aws.v4.signer.SigningException;
import uk.co.lucasweb.aws.v4.signer.Streams;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return providers.stream()
                .flatMap(p -> Streams.streamopt(p.getCredentials()))
                .findFirst()
                .orElseThrow(() -> new SigningException("no AWS credentials were provided"));
    }

    AwsCredentialsProvider environmentProvider() {
        return () -> getAwsCredentials(environmentVarResolver.getenv(ACCESS_KEY_ENV_VAR),
                                       environmentVarResolver.getenv(SECRET_KEY_ENV_VAR));
    }

    AwsCredentialsProvider systemPropertiesProvider() {
        return () -> getAwsCredentials(System.getProperty(ACCESS_KEY_SYSTEM_PROPERTY),
                                       System.getProperty(SECRET_KEY_SYSTEM_PROPERTY));
    }

    private Optional<AwsCredentials> getAwsCredentials(String accessKey, String secretKey) {

        Optional<String> optionalAccessKey = Optional.ofNullable(accessKey);
        Optional<String> optionalSecretKey = Optional.ofNullable(secretKey);
        if (optionalAccessKey.isPresent() && optionalSecretKey.isPresent()) {
            return Optional.of(new AwsCredentials(optionalAccessKey.get(), optionalSecretKey.get()));
        } else {
            return Optional.empty();
        }
    }

    static class EnvironmentVarResolver {
        String getenv(String name) {
            return System.getenv(name);
        }
    }


}
