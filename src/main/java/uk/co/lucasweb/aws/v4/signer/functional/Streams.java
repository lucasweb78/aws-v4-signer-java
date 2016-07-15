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
