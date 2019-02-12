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

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 5/5/14
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class StringUtilsTest {
    @Test
    public void testTrimLeading() throws Exception {

        String trimmed = StringUtils.trimLeading("||FOO|BAR||||", "|");
        Assert.assertEquals("FOO|BAR||||",trimmed);

        trimmed = StringUtils.trimLeading("||||FOO|BAR||||", "||");
        Assert.assertEquals("FOO|BAR||||",trimmed);

        //leave one residue
        trimmed = StringUtils.trimLeading("|||||FOO|BAR||||", "||");
        Assert.assertEquals("|FOO|BAR||||",trimmed);
    }

    @Test
    public void testTrimTrailing() throws Exception {
        String trimmed = StringUtils.trimTrailing("||FOO|BAR||||", "|");
        Assert.assertEquals("||FOO|BAR",trimmed);

        trimmed = StringUtils.trimTrailing("||||FOO|BAR||||", "||");
        Assert.assertEquals("||||FOO|BAR",trimmed);

        //leave one residue
        trimmed = StringUtils.trimTrailing("FOO|BAR|||", "||");
        Assert.assertEquals("FOO|BAR|",trimmed);
    }


}
