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
