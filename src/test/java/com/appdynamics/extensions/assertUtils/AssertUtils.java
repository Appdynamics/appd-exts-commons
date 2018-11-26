/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.assertUtils;

import org.junit.Assert;

import java.util.List;

/**
 * @author Satish Muddam
 */
public class AssertUtils extends Assert {


    public static <T> void assertOneOf(List<T> expectsOneOf, T value) {
        boolean contains = expectsOneOf.contains(value);
        if (contains) {
            return;
        } else {
            StringBuilder sb = new StringBuilder("Value received [" + value + "] is not in the expected value list [" + expectsOneOf + "]");
            fail(sb.toString());
        }
    }
}