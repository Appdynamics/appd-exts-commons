package com.appdynamics.extensions.util;

import com.appdynamics.extensions.util.StringUtils;
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
