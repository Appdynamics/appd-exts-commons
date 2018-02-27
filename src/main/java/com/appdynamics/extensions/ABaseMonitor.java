package com.appdynamics.extensions;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 *
 * An {@link AManagedMonitor} extends the core functionality of
 * AppDynamics machine agent (MA) / server infrastructure monitoring
 * (SIM) and can be implemented to perform custom tasks.
 *
 * <p>An {@code ABaseMonitor} is a wrapper on top of
 * {@link AManagedMonitor} to remove the boiler plate code of
 * creating a {@link AMonitorJob} and initializing the
 * {@link MonitorConfiguration}.
 *
 * <p>The MA or SIM agent loads all the {@link AManagedMonitor}s
 * from their respective subdirectories in the monitors directory
 * in MA or SIM. The MA or SIM agent reads the monitor.xml from
 * each of the subdirectory and loads the jars from each monitor
 * in its own classloader.
 *
 * <p>The MA or SIM calls the {@code execute} method periodically
 * every <execution-frequency-in-seconds> time duration.
 * <execution-frequency-in-seconds> is specified in the monitor.xml.
 *
 * <p>The design of {@code ABaseMonitor} supports a fan out
 * approach for the monitors i.e. you can have more than one
 * sub tasks to fetch metrics from an artifact concurrently.
 *
 * <pre> {@code
 * public class SampleMonitor extends AManagedMonitor {
 *
 * protected abstract String getDefaultMetricPrefix(){
 *     return "Custom Metrics|Sample Monitor"
 * }
 *
 * public abstract String getMonitorName(){
 *     return "Sample Monitor"
 * }
 *
 * protected abstract void doRun(TasksExecutionServiceProvider taskCounter){
 *     //...logic to add the core logic for the SampleMonitor
 * }
 *
 * protected abstract int getTaskCount(){
 *     //...number of tasks from which metrics can be pulled
 *     //concurrently.
 * }
 *
 * }}
 * </pre>
 *
 * @since 2.0
 * @author kunal.gupta
 *
 */
public abstract class ABaseMonitor extends AManagedMonitor{

    private static final Logger logger = LoggerFactory.getLogger(ABaseMonitor.class);

    /**
     * The name of the monitor.
     */
    protected String monitorName;

    /**
     * A configuration object that reads the monitor's config file
     * and initializes the different bits required by the monitor.
     */
    protected MonitorConfiguration configuration;

    /**
     * A runnable which does all the leg work for fetching the
     * metrics in a separate thread.
     */
    protected AMonitorJob monitorJob;

    public ABaseMonitor(){
        this.monitorName = getMonitorName();
        logger.info("Using {} Version [{}]",monitorName, getImplementationVersion());
    }

    protected void initialize(Map<String, String> args) {
        if(configuration == null){
            monitorJob = createMonitorJob();
            MonitorConfiguration conf = new MonitorConfiguration(monitorName,getDefaultMetricPrefix(), monitorJob);
            conf.setConfigYml(args.get("config-file"), new MonitorConfiguration.FileWatchListener() {
                @Override
                public void onFileChange(File file) {
                    onConfigReload(file);
                }
            });
            initializeMoreStuff(args, conf);
            this.configuration = conf;
        }
    }

    protected void onConfigReload(File file){};

    protected AMonitorJob createMonitorJob() {
        return new AMonitorJob(this);
    }

    /**
     * A placeholder method which should be overridden if there are
     * custom objects to be initialized in the monitor.
     * @param conf
     */
    protected void initializeMoreStuff(Map<String, String> args, MonitorConfiguration conf) {
        ;
    }

    /**
     *
     * This method is invoked by the MA or SIM agent with a
     * frequency as specified by the <execution-frequency-in-seconds>
     * field in the monitor.xml.
     * @param args
     * @param taskExecutionContext
     * @return
     * @throws TaskExecutionException
     */
    @Override
    public TaskOutput execute(Map<String, String> args, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        logger.info("Using {} Version [" + getImplementationVersion() + "]",monitorName);
        logger.debug("The raw arguments are {}",args);
        initialize(args);
        executeMonitor();
        return new TaskOutput(String.format("Monitor {} completes.",monitorName));
    }

    protected void executeMonitor() {
        if(configuration.isEnabled()){
            if(configuration.isScheduledModeEnabled()){ //scheduled mode
                logger.debug("Task scheduler is enabled, printing the metrics from the cache");
                monitorJob.printAllFromCache();
            }
            else{   // normal mode
                monitorJob.run();
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

    public MonitorConfiguration getConfiguration(){
        return configuration;
    }

    protected abstract void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider);

    protected abstract int getTaskCount();

    protected void onComplete() {
        logger.info("Finished processing all tasks in the job for {}",getMonitorName());
    }
}
