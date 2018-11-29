/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.asserts;

import com.google.common.collect.Lists;
import org.junit.Test;

/**
 * @author Satish Muddam
 */
public class CustomAssertsTest {

    @Test
    public void testAssertOneOfWithMatchedValue() {
        CustomAsserts.assertOneOf(Lists.newArrayList(1, 2, 3, 4), 3);
    }

    @Test(expected = AssertionError.class)
    public void testAssertOneOfWithNoMatchedValues() {
        CustomAsserts.assertOneOf(Lists.newArrayList(1, 2, 3, 4), 5);
    }
}