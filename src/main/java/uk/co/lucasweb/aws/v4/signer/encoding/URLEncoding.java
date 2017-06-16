/*
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  specific language governing permissions and limitations under the License.

  Copyright 2016 the original author or authors.
 */
package uk.co.lucasweb.aws.v4.signer.encoding;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * Utilities to URL-encode strings.
 * <p>
 * AWS expects most strings incorporated into the string to sign to be
 * URL-encoded, and unfortunately the exact requirements aren't exactly
 * standard:
 * <ul>
 * <li>It requires to encode '~", so {@code URLEncodedUtils} and
 * {@code URIBuilder} from Apache HTTP Client won't work.
 * <li>It requires to encode spaces as '%20', not '+', so
 * {@link java.net.URLEncoder} won't work.
 * </ul>
 *
 * @author Yoann Rodiere
 */
public class URLEncoding {

    private static final byte ESCAPE_CHAR = '%';

    private static final int HEXADECIMAL_RADIX = 16;

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final BitSet PATH_UNESCAPED_CHARACTERS;
    private static final BitSet QUERY_COMPONENT_UNESCAPED_CHARACTERS;
    static {
        /*
         * See http://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-header-based-auth.html
         * for the list of characters that should not be escaped
         */
        BitSet unreserved = new BitSet();
        for (int i = 'a'; i <= 'z'; i++) {
            unreserved.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            unreserved.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            unreserved.set(i);
        }
        unreserved.set('-');
        unreserved.set('.');
        unreserved.set('_');
        unreserved.set('~');

        PATH_UNESCAPED_CHARACTERS = new BitSet();
        PATH_UNESCAPED_CHARACTERS.set('/');
        PATH_UNESCAPED_CHARACTERS.or(unreserved);

        QUERY_COMPONENT_UNESCAPED_CHARACTERS = new BitSet();
        QUERY_COMPONENT_UNESCAPED_CHARACTERS.or( unreserved );
    }

    private URLEncoding() {
        // Private, this is a utils class
    }

    /**
     * URL-encode a path.
     * @param path The string to encode
     * @return An encoded version of the given string.
     */
    public static String encodePath(String path) {
        return encode(path, PATH_UNESCAPED_CHARACTERS);
    }

    /**
     * URL-encode the name or the value of a query parameter.
     * @param string The string to encode
     * @return An encoded version of the given string.
     */
    public static String encodeQueryComponent(String string) {
        return encode(string, QUERY_COMPONENT_UNESCAPED_CHARACTERS);
    }

    /**
     * URL-encode a String.
     */
    private static String encode(String value, BitSet unescapedChars) {
        // Code from org.apache.commons.codec.net.URLCodec.encodeUrl(BitSet, byte[])
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (final byte c : value.getBytes(CHARSET)) {
            int b = c;
            if (b < 0) {
                b = 256 + b;
            }
            if (unescapedChars.get(b)) {
                buffer.write(b);
            } else {
                buffer.write(ESCAPE_CHAR);
                final char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, HEXADECIMAL_RADIX));
                final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, HEXADECIMAL_RADIX));
                buffer.write(hex1);
                buffer.write(hex2);
            }
        }
        return new String(buffer.toByteArray(), CHARSET);
    }

}
