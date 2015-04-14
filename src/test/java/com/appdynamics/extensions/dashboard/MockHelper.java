package com.appdynamics.extensions.dashboard;

import java.lang.reflect.Field;

/**
 * Created by abey.tom on 4/13/15.
 */
public class MockHelper {
    public static void set(String fieldName, Object instance, Object value) {
        if (instance != null) {
            Class<?> clazz = instance.getClass();
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(instance,value);
            } catch (Exception e) {
                throw new RuntimeException("Error while setting the value of the field "
                        + fieldName + " of " + instance + " with value " + value, e);
            }
        }
    }
}
