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

import com.appdynamics.extensions.metrics.PerMinValueCalculator;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by abey.tom on 3/15/16.
 */
public class PerMinValueCalculatorTest {

    @Test
    public void testGetPerMinuteValue() throws Exception {
        PerMinValueCalculator agg = new PerMinValueCalculator();
        agg.getPerMinuteValue("path", new BigDecimal("100"));
        Thread.sleep(3000);
        BigDecimal perMinuteValue = agg.getPerMinuteValue("path", new BigDecimal("200"));
        System.out.println(perMinuteValue);
    }

    @Test
    public void bigDecimalTest(){
        BigDecimal decimal = new BigDecimal(100D / 3);
        System.out.println(decimal);

    }
}