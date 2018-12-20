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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.workbench.metric.WorkbenchMetricStore;

import java.util.Map;

import static com.appdynamics.extensions.conf.MonitorContext.isWorkbenchMode;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class WorkBenchModule {

    private static MetricWriteHelper workbenchStore;
    private DerivedMetricsModule derivedMetricsModule = new DerivedMetricsModule();

    public MetricWriteHelper getWorkBench(){
        return workbenchStore;
    }

    public void initWorkBenchStore(Map<String, ?> config, String metricPrefix) {
        if(isWorkbenchMode()){
            WorkbenchMetricStore.initialize(derivedMetricsModule.initDerivedMetricsCalculator(config, metricPrefix));
            workbenchStore = WorkbenchMetricStore.getInstance();
        }
    }

}
