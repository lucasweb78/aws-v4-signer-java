/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2016 the original author or authors.
 */
package uk.co.lucasweb.aws.v4.signer;

/**
 * Invokes a function wrapping any checked exceptions thrown by the function in an unchecked exception.
 *
 * @author Richard Lucas
 */
public final class Throwables {

    private Throwables() {
        // hide default constructor
    }

    @FunctionalInterface
    public interface ExceptionWrapper<E> {
        E wrap(Exception e);
    }

    @FunctionalInterface
    public interface ReturnableInstance<R> {
        R apply() throws Exception;
    }

    @FunctionalInterface
    public interface VoidInstance {
        void apply() throws Exception;
    }

    public static <E extends Throwable> void voidInstance(VoidInstance voidInstance, ExceptionWrapper<E> wrapper) throws E {
        try {
            voidInstance.apply();
        } catch (Exception e) {
            throw wrapper.wrap(e);
        }
    }

    public static <R, E extends Throwable> R returnableInstance(ReturnableInstance<R> returnableInstance, ExceptionWrapper<E> wrapper) throws E {
        try {
            return returnableInstance.apply();
        } catch (Exception e) {
            throw wrapper.wrap(e);
        }
    }


}
