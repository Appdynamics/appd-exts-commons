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
