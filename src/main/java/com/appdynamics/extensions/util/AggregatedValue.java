package com.appdynamics.extensions.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
* Created with IntelliJ IDEA.
* User: abey.tom
* Date: 5/8/14
* Time: 11:07 PM
* To change this template use File | Settings | File Templates.
*/
public class AggregatedValue {
    private BigDecimal sum;
    private long count;

    public void add(BigDecimal value) {
        if (sum != null) {
            sum = sum.add(value);
        } else {
            sum = value;
        }
        ++count;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public BigDecimal getAverage() {
        if (sum != null && count > 0) {
            return sum.divide(new BigDecimal(count),0, RoundingMode.HALF_UP);
        }
        return sum;
    }

}
