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

import java.util.TimeZone;

/**
 * Created by venkata.konala on 10/7/18.
 */
public class TimeUtilsTest {

    @Test
    public void whenAppropriateTimeAndPatternThenReturnFormattedTime() {
        Long timeInMillis = 1538962125642L;
        String timeStamp = TimeUtils.getFormattedTimestamp(timeInMillis, "yyyy-MM-dd HH:mm:ss z");
        TimeZone zone = TimeZone.getDefault();
        String displayName = zone.getDisplayName();
        if(displayName.equalsIgnoreCase("Pacific Standard Time")) {
            Assert.assertTrue(timeStamp.equals("2018-10-07 18:28:45 PDT"));
        } else {
            Assert.assertTrue(timeStamp != null && timeStamp.length() > 0);
        }
    }

    @Test
    public void whenAppropriateTimeAndPatternAndTimeZoneThenReturnFormattedTime() {
        Long timeInMillis = 1538962125642L;
        String timeStamp = TimeUtils.getFormattedTimestamp(timeInMillis, "yyyy-MM-dd HH:mm:ss z", "GMT");
        Assert.assertTrue(timeStamp.equals("2018-10-08 01:28:45 GMT"));
    }

    @Test
    public void whenAppropriateTimeAndDifferentPatternAndTimeZoneThenReturnFormattedTime() {
        Long timeInMillis = 1549496026110L;
        String timeStamp = TimeUtils.getFormattedTimestamp(timeInMillis, "yyyy-MM-dd'T'HH:mm:ss'Z'", "GMT");
        Assert.assertTrue(timeStamp.equals("2019-02-06T23:33:46Z"));
    }

    @Test
    public void whenTimeIsNullAndPatternIsAppropriateThenReturnNull() {
        Long timeInMillis = null;
        String timeStamp = TimeUtils.getFormattedTimestamp(timeInMillis, "yyyy-MM-dd HH:mm:ss z");
        Assert.assertTrue(timeStamp == null);
    }

    @Test
    public void whenTimeIsAppropriateAndPatternIsNullThenReturnNull() {
        Long timeInMillis = 1538962125642L;
        String timeStamp = TimeUtils.getFormattedTimestamp(timeInMillis, null);
        Assert.assertTrue(timeStamp == null);
    }
}
