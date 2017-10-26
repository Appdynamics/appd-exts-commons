package com.appdynamics.extensions.conf.configurationModules;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.yml.YmlReader;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class CacheModuleTest {

    @Test
    public void simpleMetricCacheTest(){
        CacheModule cacheModule = new CacheModule();
        cacheModule.initCache();
        cacheModule.putInMetricCache("Redis|Server1|ratio", new Metric("ratio", "2", "Redis|Server1|ratio"));
        Assert.assertTrue(cacheModule.getMetricCache().size() == 1);
        ConcurrentMap<String, Metric> map = cacheModule.getMetricCache().asMap();
        Assert.assertTrue(map.get("Redis|Server1|ratio").getMetricValue().equals("2"));
    }

    @Test
    public void redundantEntryTest(){
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
