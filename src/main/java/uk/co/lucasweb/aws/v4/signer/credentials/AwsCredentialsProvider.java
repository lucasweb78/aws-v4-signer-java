package uk.co.lucasweb.aws.v4.signer.credentials;

import java.util.Optional;

/**
 * Functional Interface for providing AWS credentials.
 *
 * @author Richard Lucas
 */
@FunctionalInterface
public interface AwsCredentialsProvider {

    Optional<AwsCredentials> getCredentials();
}
