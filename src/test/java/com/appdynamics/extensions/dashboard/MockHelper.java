/*
 * Copyright (c) 2019 AppDynamics,Inc.
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
