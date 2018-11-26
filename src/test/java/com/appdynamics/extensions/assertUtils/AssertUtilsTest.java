/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.assertUtils;

import com.google.common.collect.Lists;
import org.junit.Test;

/**
 * @author Satish Muddam
 */
public class AssertUtilsTest {

    @Test
    public void testAssertOneOfWithMatchedValue() {
        AssertUtils.assertOneOf(Lists.newArrayList(1, 2, 3, 4), 3);
    }

    @Test(expected = AssertionError.class)
    public void testAssertOneOfWithNoMatchedValues() {
        AssertUtils.assertOneOf(Lists.newArrayList(1, 2, 3, 4), 5);
    }
}