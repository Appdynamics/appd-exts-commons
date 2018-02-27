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

package com.appdynamics.extensions.workbench.ui;

import com.appdynamics.extensions.util.JsonUtils;
import com.appdynamics.extensions.workbench.metric.WorkbenchMetricStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Created by abey.tom on 3/16/16.
 */
public class MetricTreeBuilder {
    public static final Logger logger = LoggerFactory.getLogger(MetricTreeBuilder.class);
    private WorkbenchMetricStore metricStore;

    public MetricTreeBuilder(WorkbenchMetricStore metricStore) {
        this.metricStore = metricStore;
    }

    public List<MetricTree> getMetricTree() {
        Set<String> paths = metricStore.getMetricPaths();
        MetricTree root = new MetricTree("root");
        if (paths != null) {
            for (String path : paths) {
                String[] split = path.split("\\|");
                root.add(split, path);
            }
        }
        return root.getNodes();
    }

    public String metricTreeAsJson() {
        List<MetricTree> metricTree = getMetricTree();
        if (metricTree != null) {
            return JsonUtils.asJson(metricTree);
        }
        return "[]";
    }
}
