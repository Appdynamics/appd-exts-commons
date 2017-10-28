package com.appdynamics.extensions.conf.configurationModules;

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
