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

package com.appdynamics.extensions.metrics;

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
