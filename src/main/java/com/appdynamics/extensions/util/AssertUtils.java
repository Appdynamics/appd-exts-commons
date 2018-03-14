/*
 * Copyright (c) 2018 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.util;

import com.google.common.base.Strings;

/**
 * Created by abey.tom on 3/15/16.
 */
public class AssertUtils {
    public static void assertNotNull(Object o, String message) {
        if (o instanceof String) {
            String str = (String) o;
            if (Strings.isNullOrEmpty(str)) {
                throw new ValidationException(str);
            }
        } else if (o == null) {
            throw new ValidationException(message);
        }
    }

    private static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
