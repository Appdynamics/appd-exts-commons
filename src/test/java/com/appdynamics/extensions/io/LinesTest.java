package com.appdynamics.extensions.io;


import com.appdynamics.extensions.http.Response;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/21/14
 * Time: 7:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinesTest {

    @Test
    public void testWithCloseCallback() {
        InputStream in = getClass().getResourceAsStream("/lines.txt");
        final AtomicInteger status = new AtomicInteger(0);
        Lines lines = new Lines(in, new Response.CloseCallback() {
            public void onClose() {
                status.incrementAndGet();
            }
        });
        int count = 0;
        for (String line : lines) {
            if (count == 3) {
                Assert.assertEquals("   .", line);
            } else if (count == 5) {
                Assert.assertEquals("", line);
            } else {
                Assert.assertEquals("line " + count, line);
            }
            ++count;
        }
        Assert.assertEquals(1, status.intValue());
    }

    /**
     * Just to make sure that there are no NPE
     */
    @Test
    public void testWithoutCloseCallback() {
        InputStream in = getClass().getResourceAsStream("/lines.txt");
        final AtomicInteger status = new AtomicInteger(0);
        Lines lines = new Lines(in);
        for (String line : lines) {

        }
    }
}
