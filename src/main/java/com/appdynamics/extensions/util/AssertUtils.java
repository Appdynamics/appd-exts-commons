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
