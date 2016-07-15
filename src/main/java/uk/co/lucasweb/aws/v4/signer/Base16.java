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

import java.io.UnsupportedEncodingException;

/**
 * @author Richard Lucas
 */
public final class Base16 {

    private static final char[] ENC_TAB = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private Base16() {
        // hide default constructor
    }

    public static String encode(String data) {
        return Throwables.returnableInstance(() -> encode(getBytes(data)), SigningException::new);
    }

    public static String encode(byte[] data) {
        int length = data.length;
        StringBuilder stringBuilder = new StringBuilder(length * 2);
        int i = 0;
        while (i < length) {
            stringBuilder.append(ENC_TAB[(data[i] & 0xF0) >> 4]);
            stringBuilder.append(ENC_TAB[data[i] & 0x0F]);
            i++;
        }

        return stringBuilder.toString();
    }

    private static byte[] getBytes(String data) throws UnsupportedEncodingException {
        return data.getBytes("UTF-8");
    }
}
