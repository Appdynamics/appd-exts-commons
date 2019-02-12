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

import java.text.SimpleDateFormat;
import java.util.TimeZone;


/**
 * Created by venkata.konala on 9/6/18.
 */
public class TimeUtils {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(TimeUtils.class);

    public static String getFormattedTimestamp(Long timeInMilli, String pattern) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            return simpleDateFormat.format(timeInMilli);
        } catch (Exception e) {
            logger.error("The time " + timeInMilli + " cannot be formatted to the pattern " + pattern);
        }
        return null;
    }

    public static String getFormattedTimestamp(Long timeInMilli, String pattern, String timeZone) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            return simpleDateFormat.format(timeInMilli);
        } catch (Exception e) {
            logger.error("The time " + timeInMilli + " cannot be formatted to the pattern " + pattern + " and timeZone " + timeZone);
        }
        return null;
    }
}