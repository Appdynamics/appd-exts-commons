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

import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.file.FileWatchListener;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.util.TimeUtils;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * An {@link AManagedMonitor} extends the core functionality of
 * AppDynamics machine agent (MA) / server infrastructure monitoring
 * (SIM) and can be implemented to perform custom tasks.
 *
 * <p>An {@code ABaseMonitor} is a wrapper on top of
 * {@link AManagedMonitor} to remove the boiler plate code of
 * creating a {@link AMonitorJob} and initializing the
 * {@link MonitorContextConfiguration}.
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
 * public class SampleMonitor extends ABaseMonitor {
 *
 * protected abstract String getDefaultMetricPrefix(){
 *     return "Custom Metrics|Sample Monitor"
 * }
 *
 * public abstract String getMonitorName(){
 *     return "Sample Monitor"
 * }
 *
 * protected abstract void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider){
 *     //...logic to add the core logic for the SampleMonitor
 * }
 *
 * protected abstract List<Map<>> getServers(){
 *     //list of servers from the config.yml
 *     // to keep track of tasks to be processed in parallel
 *
 * }
 *
 * }}
 * </pre>
 *
 * @author kunal.gupta
 * @since 2.0
 */
public abstract class ABaseMonitor extends AManagedMonitor {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ABaseMonitor.class);

    private Long startTime;
    /**
     * The name of the monitor.
     */
    protected String monitorName;

    private File installDir;

    /**
     * A contextConfiguration object that reads the monitor's config file
     * and initializes the different bits required by the monitor.
     */
    private MonitorContextConfiguration contextConfiguration;

    /**
     * A runnable which does all the leg work for fetching the
     * metrics in a separate thread.
     */
    protected AMonitorJob monitorJob;

    public ABaseMonitor() {
        this.monitorName = getMonitorName();
        logger.info("Using {} Version [{}]", monitorName, getImplementationVersion());
        ExtensionName.setName(monitorName);
    }

    protected void initialize(final Map<String, String> args) {
        if (contextConfiguration == null) {
            installDir = getInstallDirectory();
            monitorJob = createMonitorJob();
            contextConfiguration = createContextConfiguration();
            contextConfiguration.registerListener(args.get("config-file"), createYmlFileListener(args.get("config-file")));
            MetricPathUtils.registerMetricCharSequenceReplacer(this);
            initializeMoreStuff(args);
        }
    }

    private FileWatchListener createYmlFileListener(final String ymlFile) {
        FileWatchListener fileWatchListener = new FileWatchListener() {
            @Override
            public void onFileChange(File file) {
                contextConfiguration.setConfigYml(ymlFile);
                onConfigReload(file);
            }
        };
        return fileWatchListener;
    }

    private File getInstallDirectory() {
        File installDir = PathResolver.resolveDirectory(AManagedMonitor.class);
        if (installDir == null) {
            throw new RuntimeException("The install directory cannot be null");
        }
        return installDir;
    }

    private MonitorContextConfiguration createContextConfiguration() {
        return new MonitorContextConfiguration(getMonitorName(), getDefaultMetricPrefix(), installDir, monitorJob);
    }

    protected void onConfigReload(File file) {
    }

    ;

    protected AMonitorJob createMonitorJob() {
        return new AMonitorJob(this);
    }

    /**
     * A placeholder method which should be overridden if there are
     * custom objects to be initialized in the monitor.
     *
     * @param args
     */
    protected void initializeMoreStuff(Map<String, String> args) {
        ;
    }

    /**
     * This method is invoked by the MA or SIM agent with a
     * frequency as specified by the <execution-frequency-in-seconds>
     * field in the monitor.xml.
     *
     * @param args
     * @param taskExecutionContext
     * @return
     * @throws TaskExecutionException
     */
    @Override
    public TaskOutput execute(Map<String, String> args, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        startTime = System.currentTimeMillis();
        logger.info("Started executing " + monitorName + " at " + TimeUtils.getFormattedTimestamp(startTime, "yyyy-MM-dd HH:mm:ss z"));
        logger.info("Using {} Version [" + getImplementationVersion() + "]", monitorName);
        logger.debug("The raw arguments are {}", args);
        initialize(args);
        executeMonitor();
        return new TaskOutput(String.format("Monitor %s completes.", monitorName));
    }

    protected void executeMonitor() {
        if (contextConfiguration.isEnabled()) {
            MonitorContext context = contextConfiguration.getContext();
            AssertUtils.assertNotNull(context, "The context of the extension has not been initialised!!!! Please check your contextConfiguration");
            if (context.isScheduledModeEnabled()) { //scheduled mode
                logger.debug("Task scheduler is enabled, printing the metrics from the cache");
                monitorJob.printAllFromCache();
            } else {   // normal mode
                monitorJob.run();
            }
        } else {
            logger.debug("The monitor [{}] is not enabled.", getMonitorName());
        }
    }

    protected static String getImplementationVersion() {
        return ABaseMonitor.class.getPackage().getImplementationTitle();
    }

    public Long getStartTime() {
        return startTime;
    }

    protected abstract String getDefaultMetricPrefix();

    public abstract String getMonitorName();

    public MonitorContextConfiguration getContextConfiguration() {
        return contextConfiguration;
    }

    protected abstract void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider);

    protected abstract List<Map<String, ?>> getServers();

    protected void onComplete() {
        logger.info("Finished processing all tasks in the job for {}", getMonitorName());
    }

}
