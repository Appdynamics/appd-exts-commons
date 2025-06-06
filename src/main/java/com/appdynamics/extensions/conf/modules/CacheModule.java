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
