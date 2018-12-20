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

package com.appdynamics.extensions;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.workbench.metric.WorkbenchMetricStore;
import org.slf4j.Logger;

import static com.appdynamics.extensions.conf.MonitorContext.isWorkbenchMode;

/**
 * Created by abey.tom on 3/20/16.
 */
public class MetricWriteHelperFactory {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricWriteHelperFactory.class);

    public static MetricWriteHelper create(ABaseMonitor baseMonitor) {
        MetricWriteHelper helper;
        if (isWorkbenchMode()) {
            helper = WorkbenchMetricStore.getInstance();
        } else {
            helper = new MetricWriteHelper(baseMonitor);
        }
        logger.info("The instance of MetricWriteHelperFactory is {}", helper);
        return helper;
    }
}
