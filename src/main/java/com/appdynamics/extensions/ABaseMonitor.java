package com.appdynamics.extensions;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This is a base class that each monitor can extend from.
 */
public abstract class ABaseMonitor extends AManagedMonitor{

    private static final Logger logger = LoggerFactory.getLogger(ABaseMonitor.class);
    protected String monitorName;
    protected MonitorConfiguration configuration;
    protected AMonitorTaskRunner monitorTaskRunner;

    public ABaseMonitor(){
        this.monitorName = getMonitorName();
        logger.info("Using {} Version [{}]",monitorName, getImplementationVersion());
    }

    protected void initialize(Map<String, String> args) {
        if(configuration == null){
            monitorTaskRunner = createMonitorTask();
            MonitorConfiguration conf = new MonitorConfiguration(monitorName,getDefaultMetricPrefix(),monitorTaskRunner);
            conf.setConfigYml(args.get("config-file"));
            initializeMoreStuff(conf);
            this.configuration = conf;
        }
    }

    protected AMonitorTaskRunner createMonitorTask() {
        return new AMonitorTaskRunner(this);
    }

    /**
     * Should be overridden to initialize more stuff.
     * @param conf
     */
    protected void initializeMoreStuff(MonitorConfiguration conf) {
        ;
    }

    @Override
    public TaskOutput execute(Map<String, String> args, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        logger.debug("Monitor {} is invoked.",monitorName);
        logger.debug("The raw arguments are {}",args);
        initialize(args);
        executeMonitor();
        return new TaskOutput(String.format("Monitor {} completes.",monitorName));
    }

    protected void executeMonitor() {
        if(configuration.isEnabled()){
            if(configuration.isScheduledModeEnabled()){ //scheduled mode
                logger.debug("Task scheduler is enabled, printing the metrics from the cache");
                monitorTaskRunner.printAllFromCache();
            }
            else{   // normal mode
                monitorTaskRunner.run();
            }
        } else{
            logger.debug("The monitor [{}] is not enabled.", getMonitorName());
        }
    }

    protected static String getImplementationVersion() {
        return ABaseMonitor.class.getPackage().getImplementationTitle();
    }

    protected abstract String getDefaultMetricPrefix();

    public abstract String getMonitorName();

    protected abstract void doRun(AMonitorRunContext taskCounter);

    protected abstract int getTaskCount();

    protected void onComplete() {
        logger.info("Finished processing all tasks for {}",getMonitorName());
    }
}
