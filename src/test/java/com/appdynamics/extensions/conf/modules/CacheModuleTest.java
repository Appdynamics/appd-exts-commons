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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.metrics.Metric;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class CacheModuleTest {

    @Test
    public void metricCacheEntryExistsCheckAfterEntryHasBeenAddedTest(){
        CacheModule cacheModule = new CacheModule();
        cacheModule.initCache();
        cacheModule.putInMetricCache("Redis|Server1|ratio", new Metric("ratio", "2", "Redis|Server1|ratio"));
        Assert.assertTrue(cacheModule.getMetricCache().size() == 1);
        ConcurrentMap<String, Metric> map = cacheModule.getMetricCache().asMap();
        Assert.assertTrue(map.get("Redis|Server1|ratio").getMetricValue().equals("2"));
    }

    @Test
    public void whenRedundantEntriesWithSameKeysLatestEntryOverridesTheOldEntryTest(){
        CacheModule cacheModule = new CacheModule();
        cacheModule.initCache();
        cacheModule.putInMetricCache("Redis|Server1|ratio", new Metric("ratio", "2", "Redis|Server1|ratio"));
        Assert.assertTrue(cacheModule.getMetricCache().size() == 1);
        cacheModule.putInMetricCache("Redis|Server1|ratio", new Metric("ratio", "4", "Redis|Server1|ratio"));
        Assert.assertTrue(cacheModule.getMetricCache().size() == 1);
        ConcurrentMap<String, Metric> map = cacheModule.getMetricCache().asMap();
        Assert.assertTrue(map.get("Redis|Server1|ratio").getMetricValue().equals("4"));

    }
}
