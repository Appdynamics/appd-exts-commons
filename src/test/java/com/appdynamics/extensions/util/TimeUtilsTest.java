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
