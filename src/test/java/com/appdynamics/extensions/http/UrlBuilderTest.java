package com.appdynamics.extensions.http;

import com.appdynamics.extensions.TaskInputArgs;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/7/14
 * Time: 10:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class UrlBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullArguments(){
        UrlBuilder.builder(null);
    }

    @Test
    public void noHost(){
        HashMap<String, String> map = new HashMap<String, String>();
        String url = new UrlBuilder(map).path("/test").build();
        Assert.assertEquals("http://localhost:80/test", url);
    }

    @Test
    public void test(){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TaskInputArgs.USE_SSL,"true");
        map.put(TaskInputArgs.HOST,"192.168.57.101");
        map.put(TaskInputArgs.PORT,"1234");
        String url = new UrlBuilder(map).build();
        Assert.assertEquals("https://192.168.57.101:1234", url);
    }

    @Test
    public void testPathAndQueryString(){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TaskInputArgs.USE_SSL,"false");
        map.put(TaskInputArgs.HOST,"192.168.57.101");
        map.put(TaskInputArgs.PORT,"1234");
        String url = new UrlBuilder(map)
                .path("path1/")
                .path("/path2")
                .query("key1","value1")
                .build();
        Assert.assertEquals("http://192.168.57.101:1234/path1/path2?key1=value1", url);
    }

    @Test
    public void testPathAndQueryNoValue(){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TaskInputArgs.USE_SSL,"false");
        map.put(TaskInputArgs.HOST,"192.168.57.101");
        map.put(TaskInputArgs.PORT,"1234");
        String url = new UrlBuilder(map)
                .path("path1////")
                .path("/////////path2")
                .query("key1","value1")
                .query("key2=value2")
                .build();
        Assert.assertEquals("http://192.168.57.101:1234/path1/path2?key1=value1&key2=value2", url);
    }

}
