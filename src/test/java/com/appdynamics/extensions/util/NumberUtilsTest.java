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

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 10/27/17.
 */
public class NumberUtilsTest {
    @Test
    public void bigIntegerIsNumberTest(){
        BigDecimal num = new BigDecimal(Double.MAX_VALUE + 10);
        Assert.assertTrue(NumberUtils.isNumber(num.toString()));
    }

    @Test
    public void whenNanThenReturnFalse(){
        String naN = "NaN";
        Assert.assertFalse(NumberUtils.isNumber(naN));
    }

    @Test
    public void whenInfinityThenReturnFalse(){
        String positiveInfinity = "Infinity";
        String negativeInfinity = "-Infinity";
        Assert.assertFalse("Dropiing Infinity", NumberUtils.isNumber(positiveInfinity));
        Assert.assertFalse("Dropiing _Inifinity", NumberUtils.isNumber(negativeInfinity) );
    }
}
