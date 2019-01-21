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

package com.appdynamics.extensions.util;

import com.appdynamics.extensions.controller.ControllerInfo;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by venkata.konala on 1/11/19.
 */
public class ValidationUtilsTest {

    @Test
    public void validStringTest() {
        Assert.assertTrue(ValidationUtils.isValidString("sample"));
        Assert.assertTrue(ValidationUtils.isValidString("sample1234"));
        Assert.assertTrue(ValidationUtils.isValidString("1234-=9"));
        Assert.assertFalse(ValidationUtils.isValidString(""));
        Assert.assertFalse(ValidationUtils.isValidString(null));
    }

    @Test
    public void validMetricValueTest() {
        Assert.assertTrue(ValidationUtils.isValidMetricValue("1"));
        Assert.assertTrue(ValidationUtils.isValidMetricValue("1.05"));
        Assert.assertFalse(ValidationUtils.isValidMetricValue("-1"));
        Assert.assertFalse(ValidationUtils.isValidMetricValue(null));
        Assert.assertFalse(ValidationUtils.isValidMetricValue("notANumber"));
    }

    @Test
    public void validMetricPathTest() {
        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        when(controllerInfo.getSimEnabled()).thenReturn(false);
        Assert.assertTrue(ValidationUtils.isValidMetricPath(controllerInfo, "Server|Component:123|Custom Metrics|Redis Monitor|test"));
        Assert.assertFalse(ValidationUtils.isValidMetricPath(controllerInfo, "Server|Component:123|Custom Metrics|Redis Monitor|test|"));
        Assert.assertFalse(ValidationUtils.isValidMetricPath(controllerInfo, "Server|Component:123|Custom Metrics|Redis,Monitor|test"));
        Assert.assertFalse(ValidationUtils.isValidMetricPath(controllerInfo, "Server|Component:123|Custom Metrics|Redis Monitor||test"));
    }

    @Test
    public void validMetricPrefixWhenSIMEnabledTest() {
        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        when(controllerInfo.getSimEnabled()).thenReturn(true);
        Assert.assertFalse(ValidationUtils.isValidMetricPrefix(controllerInfo, "Server|Component:123|Custom Metrics|Redis Monitor|test"));
        Assert.assertTrue(ValidationUtils.isValidMetricPrefix(controllerInfo, "Custom Metrics|Redis Monitor|test"));

    }

    @Test
    public void validMetricPrefixWhenSIMNotEnabledTest() {
        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        when(controllerInfo.getSimEnabled()).thenReturn(false);
        Assert.assertTrue(ValidationUtils.isValidMetricPrefix(controllerInfo, "Server|Component:123|Custom Metrics|Redis Monitor|test"));
        Assert.assertFalse(ValidationUtils.isValidMetricPrefix(controllerInfo, "Server|Component:123|Agent|Redis Monitor|test"));
        Assert.assertFalse(ValidationUtils.isValidMetricPrefix(controllerInfo, "Server|Component:<TIER ID>|Redis Monitor|test"));
    }
}
