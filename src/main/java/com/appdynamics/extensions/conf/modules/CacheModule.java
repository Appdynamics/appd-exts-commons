package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.metrics.Metric;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import java.util.concurrent.TimeUnit;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class CacheModule {

    private Cache<String, MetricWriter> writerCache;
    private Cache<String, Metric> metricCache;

    public Cache<String, Metric> getMetricCache(){
        return metricCache;
    }

    public void putInMetricCache(String metricPath, Metric metric){
        metricCache.put(metricPath, metric);
    }

    public Cache<String, MetricWriter> getWriterCache(){
        return writerCache;
    }

    public void putInWriterCache(String metricPath, MetricWriter writer){
        writerCache.put(metricPath, writer);
    }

    public void initCache() {
        writerCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
        metricCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).build();
    }


}
