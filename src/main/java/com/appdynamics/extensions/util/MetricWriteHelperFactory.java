package com.appdynamics.extensions.util;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.workbench.metric.WorkbenchMetricStore;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by abey.tom on 3/20/16.
 */
public class MetricWriteHelperFactory {
    public static final Logger logger = LoggerFactory.getLogger(MetricWriteHelperFactory.class);

    public static MetricWriteHelper create(AManagedMonitor monitor) {
        MetricWriteHelper helper;
        if (MonitorConfiguration.isWorkbenchMode()) {
            helper = WorkbenchMetricStore.getInstance();
        } else {
            helper = new MetricWriteHelper(monitor);
        }
        logger.info("The instance of MetricWriteHelper is {}", helper);
        return helper;
    }
}
