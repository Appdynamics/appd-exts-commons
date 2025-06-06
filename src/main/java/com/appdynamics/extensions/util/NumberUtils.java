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

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/7/14
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class NumberUtils {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(NumberUtils.class);

    public static boolean isNumber(String str) {
         {
             if(str !=  null) {
                 str = str.trim();
                 try {
                     Double d = Double.parseDouble(str);
                     return Double.isFinite(d);
                 } catch (Exception e) {
                     logger.warn("Unable to validate the value as a valid number", e);
                 }
             }
        }
        return false;
    }

    /*
      Please make sure String str is a valid number before passing it to this method.
     */
    public static boolean isNegative(String str){
        if(Double.parseDouble(str) < 0){
            return true;
        }
        return false;
    }

    public static String roundToWhole(BigDecimal value){
        return value.setScale(0, RoundingMode.HALF_UP).toBigInteger().toString();
    }
}
