package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.workbench.metric.WorkbenchMetricStore;
import java.util.Map;
import static com.appdynamics.extensions.conf.MonitorConfiguration.isWorkbenchMode;

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
