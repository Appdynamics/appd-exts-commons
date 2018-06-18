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

package com.appdynamics.extensions.workbench;

import static com.appdynamics.extensions.conf.MonitorContext.EXTENSION_WORKBENCH_MODE;

import com.appdynamics.extensions.conf.monitorxml.Argument;
import com.appdynamics.extensions.conf.monitorxml.Monitor;
import com.appdynamics.extensions.conf.monitorxml.TaskArguments;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.JsonUtils;
import com.appdynamics.extensions.workbench.metric.WorkbenchMetricStore;
import com.appdynamics.extensions.workbench.ui.MetricTreeBuilder;
import com.appdynamics.extensions.workbench.util.MimeTypes;
import fi.iki.elonen.NanoHTTPD;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by abey.tom on 3/16/16.
 */
public class WorkBenchServer extends NanoHTTPD {
    public static Logger logger;

    private WorkbenchMetricStore metricStore;
    private MetricTreeBuilder treeBuilder;

    public WorkBenchServer(String hostname, int port, WorkbenchMetricStore metricStore) {
        super(hostname, port);
        this.metricStore = metricStore;
        this.treeBuilder = new MetricTreeBuilder(metricStore);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if (uri.isEmpty() || uri.equals("/")) {
            Response response = newFixedLengthResponse(Response.Status.REDIRECT, "text/plain", "");
            response.addHeader("location", "workbench/index.html");
            return response;
        } else if (uri.startsWith("/workbench")) {
            String mimeType = getMimeType(uri);
            InputStream in = getResourceAsStream(uri);
            return newChunkedResponse(Response.Status.OK, mimeType, in);
        } else if (uri.startsWith("/api/metric-tree")) {
            return newFixedLengthResponse(Response.Status.OK, "application/json", treeBuilder.metricTreeAsJson());
        } else if (uri.startsWith("/api/metric-paths")) {
            String paths;
            String contentType;
            if (isContentTypeJson(session)) {
                paths = JsonUtils.asJson(metricStore.getMetricPaths());
                contentType = "application/json";
            } else {
                paths = metricStore.getMetricPathsAsPlainStr();
                contentType = "text/plain";
            }
            return newFixedLengthResponse(Response.Status.OK, contentType, paths);
        } else if (uri.startsWith("/api/metric-data")) {
            Map<String, String> parms = session.getParms();
            String metricPath = parms.get("metric-path");
            String countStr = parms.get("count");
            int count = 10;
            if (countStr != null && !countStr.trim().isEmpty()) {
                count = Integer.parseInt(countStr);
            }
            List<WorkbenchMetricStore.MetricData> metricData = metricStore.getMetricData(metricPath);
            return newFixedLengthResponse(Response.Status.OK, "application/json", JsonUtils.asJson(metricData));
        } else if (uri.startsWith("/api/stats")) {
            String content;
            String contentType;
            if (isContentTypeJson(session)) {
                content = JsonUtils.asJson(metricStore.getStats());
                contentType = "application/json";
            } else {
                content = metricStore.getStatsAsStr();
                contentType = "text/plain";
            }
            return newFixedLengthResponse(Response.Status.OK, contentType, content);
        } else {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", "");
        }

    }

    private boolean isContentTypeJson(IHTTPSession session) {
        String accept = session.getHeaders().get("accept");
        return accept != null && accept.contains("json");
    }

    protected InputStream getResourceAsStream(String uri) {
        return getClass().getResourceAsStream(uri);
    }

    private String getMimeType(String uri) {
        int i = uri.lastIndexOf(".");
        if (i != -1) {
            String type = MimeTypes.getType(uri.substring(i));
            if (type == null) {
                type = "text/plain";
            }
            return type;
        }
        return "text/plain";

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File extensionDir = resolveDirectory(WorkBenchServer.class);
        if (extensionDir != null) {
            configureLogging(extensionDir, args);
            logger = ExtensionsLoggerFactory.getLogger(WorkBenchServer.class);
            bootstrap(args, extensionDir);
        } else {
            System.out.println("[ERROR] Cannot resolve the install Directory");
        }
    }

    private static void configureLogging(File extensionDir, String[] args) throws IOException {
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        PatternLayout layout = new PatternLayout("%d{ABSOLUTE} %5p [%t] %c{1} - %m%n");
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        consoleAppender.setThreshold(Level.INFO);
        RollingFileAppender fileAppender = new RollingFileAppender();
        File machineDir = extensionDir.getParentFile().getParentFile();
        File file = new File(machineDir, "logs/workbench.log");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        fileAppender.setFile(file.getAbsolutePath(), true, false, 8 * 1024);
        fileAppender.setLayout(layout);
        fileAppender.setMaxBackupIndex(5);
        fileAppender.setMaxFileSize("10MB");


        root.addAppender(consoleAppender);
        root.addAppender(fileAppender);
        root.setLevel(Level.INFO);

        org.apache.log4j.Logger appDlogger = org.apache.log4j.Logger.getLogger("com.appdynamics");
        appDlogger.addAppender(consoleAppender);
        appDlogger.setLevel(Level.DEBUG);
        appDlogger.setAdditivity(false);
        appDlogger.addAppender(fileAppender);


        Properties properties = System.getProperties();
        for (Object o : properties.keySet()) {
            String name = (String) o;
            if (name.startsWith("workbench.logger.")) {
                String value = (String) properties.get(o);
                String logger = name.substring(17);
                System.out.println(logger);
                org.apache.log4j.Logger extLogger = org.apache.log4j.Logger.getLogger(logger);
                extLogger.addAppender(consoleAppender);
                if ("trace".equalsIgnoreCase(value)) {
                    extLogger.setLevel(Level.TRACE);
                } else if ("debug".equalsIgnoreCase(value)) {
                    extLogger.setLevel(Level.DEBUG);
                } else if ("info".equalsIgnoreCase(value)) {
                    extLogger.setLevel(Level.INFO);
                } else if ("warn".equalsIgnoreCase(value)) {
                    extLogger.setLevel(Level.WARN);
                } else if ("error".equalsIgnoreCase(value)) {
                    extLogger.setLevel(Level.ERROR);
                } else if ("fatal".equalsIgnoreCase(value)) {
                    extLogger.setLevel(Level.FATAL);
                } else if ("off".equalsIgnoreCase(value)) {
                    extLogger.setLevel(Level.OFF);
                }
                extLogger.setAdditivity(false);
                extLogger.addAppender(fileAppender);
            }
        }
//        org.apache.log4j.Logger extLogger = org.apache.log4j.Logger.getLogger("org.apache.http.headers");
//        extLogger.addAppender(consoleAppender);
//        extLogger.setLevel(Level.DEBUG);
//        extLogger.setAdditivity(false);
//        extLogger.addAppender(fileAppender);

    }

    public static void bootstrap(String[] args, File extensionDir) throws IOException {
        final Monitor monitor = Monitor.from(extensionDir);
        final Map<String, String> taskArgs = getTaskArgs(monitor);
        final Object implClass = getImplClassInstance(monitor);
        if (implClass != null) {
            System.setProperty(EXTENSION_WORKBENCH_MODE, "true");
            String host = "0.0.0.0";
            int port = 9090;
            if (args.length > 0) {
                host = args[0];
            }
            if (args.length > 1) {
                port = Integer.parseInt(args[1]);
            }
            long interval = 30;
            if (args.length > 2) {
                interval = Integer.parseInt(args[2]);
            }
            WorkbenchMetricStore metricStore = WorkbenchMetricStore.getInstance();
            System.out.println("*********************************************************************");
            System.out.println("****");
            System.out.println("****\t Starting Server on http://" + host + ":" + port);
            System.out.println("****");
            System.out.println("*********************************************************************");
            WorkBenchServer workBenchServer = new WorkBenchServer(host, port, metricStore);
            workBenchServer.start(5000, false);
            metricStore.setResetListener(new WorkbenchMetricStore.ResetListener() {
                public void onReset() {
                    logger.info("Running the Task on Reset");
                    executeTask(implClass, taskArgs);
                }
            });
            runTask(implClass, taskArgs, monitor, interval);
        }
    }


    private static void runTask(Object implClass, Map<String, String> taskArgs, Monitor monitor, long interval) {
        while (true) {
            logger.info("Executing the class " + implClass);
            executeTask(implClass, taskArgs);
            try {
                Thread.sleep(interval * 1000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }

    public static void executeTask(Object implClass, Map<String, String> taskArgs) {
        Class<?> clazz = implClass.getClass();
        try {
            Class<?> taskExecutionContextClass = clazz.getClassLoader().loadClass("com.singularity.ee.agent.systemagent.api.TaskExecutionContext");
            java.lang.reflect.Method method = clazz.getMethod("execute", Map.class, taskExecutionContextClass);
            method.invoke(implClass, taskArgs, null);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    private static Object getImplClassInstance(Monitor monitor) {
        String implClass = monitor.getMonitorRunTask().getJavaTask().getImplClass();
        if (implClass != null) {
            try {
                Class<?> monitorClazz = Thread.currentThread().getContextClassLoader().loadClass(implClass.trim());
                return monitorClazz.newInstance();
            } catch (Exception e) {
                logger.error("Cannot create an instance of the class " + implClass, e);
            }
        } else {
            logger.error("Cannot find the implementation class from the monitor.xml" + implClass);
        }
        return null;
    }

    private static Map<String, String> getTaskArgs(Monitor monitor) {
        Map<String, String> argsMap = new HashMap<String, String>();
        TaskArguments taskArguments = monitor.getMonitorRunTask().getTaskArguments();
        if (taskArguments != null) {
            Argument[] arguments = taskArguments.getArguments();
            if (arguments != null) {
                for (Argument argument : arguments) {
                    argsMap.put(argument.getName(), argument.getDefaultValue());
                }
            }
        }
        return argsMap;

    }

    public static File resolveDirectory(Class clazz) {
        File installDir = null;
        try {
            ProtectionDomain pd = clazz.getProtectionDomain();
            if (pd != null) {
                CodeSource cs = pd.getCodeSource();
                if (cs != null) {
                    URL url = cs.getLocation();
                    if (url != null) {
                        String path = URLDecoder.decode(url.getFile(), "UTF-8");
                        File dir = new File(path).getParentFile();
                        if (dir.exists()) {
                            installDir = dir;
                        } else {
                            System.err.println("Install dir resolved to " + dir.getAbsolutePath() + ", however it doesnt exist.");
                        }
                    }
                } else {
                    System.err.println("Cannot resolve path for the class " + clazz.getName() + " since CodeSource is null");
                }

            }
        } catch (Exception e) {
            System.err.println("Error while resolving the Install Dir");
            e.printStackTrace();
        }
        if (installDir != null) {
            System.out.println("Install dir resolved to " + installDir.getAbsolutePath());
            return installDir;
        } else {
            File workDir = new File("");
            System.out.println("Failed to resolve install dir, returning current work dir" + workDir.getAbsolutePath());
            return workDir;
        }
    }

    protected WorkbenchMetricStore getMetricStore() {
        return metricStore;
    }


}
