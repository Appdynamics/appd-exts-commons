/*
 * Copyright (c) 2019 AppDynamics,Inc.
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

import com.appdynamics.extensions.conf.monitorxml.Argument;
import com.appdynamics.extensions.conf.monitorxml.Monitor;
import com.appdynamics.extensions.conf.monitorxml.TaskArguments;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.JsonUtils;
import com.appdynamics.extensions.workbench.metric.WorkbenchMetricStore;
import com.appdynamics.extensions.workbench.ui.MetricTreeBuilder;
import com.appdynamics.extensions.workbench.util.MimeTypes;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.SystemPropertyConstants.WORKBENCH_MODE_PROPERTY;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Created by abey.tom on 3/16/16.
 */
public class WorkBenchServer extends Handler.Abstract {
    public static Logger logger;

    private WorkbenchMetricStore metricStore;
    private MetricTreeBuilder treeBuilder;
    private String hostname;
    private int port;

    public WorkBenchServer(String hostname, int port, WorkbenchMetricStore metricStore) {
        this.hostname=hostname;
        this.port=port;
        this.metricStore = metricStore;
        this.treeBuilder = new MetricTreeBuilder(metricStore);
    }
    public void Start() throws Exception{
        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        Server server = new Server(inetSocketAddress);
        server.setHandler(this);
        server.start();
    }
    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception{
        String uri = request.getHttpURI().getPath();
        if(uri.isEmpty() || uri.equals("/")) {
            response.setStatus(302);
            response.getHeaders().add("Location","workbench/index.html");
        } else if (uri.startsWith("/workbench")) {
            String mimeType = getMimeType(uri);
            InputStream in = getResourceAsStream(uri);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getHeaders().add(HttpHeader.CONTENT_TYPE, mimeType);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                response.write(true, ByteBuffer.wrap(new byte[0]), callback);
            }
            in.close();
        } else if (uri.startsWith("/api/metric-tree")) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getHeaders().add(HttpHeader.CONTENT_TYPE,"application/json");
            String jsonPayload = treeBuilder.metricTreeAsJson();
            response.write(true, ByteBuffer.wrap(jsonPayload.getBytes()), callback);
        } else if (uri.startsWith("/api/metric-paths")) {
            String paths;
            String contentType;
            if (isContentTypeJson(request)) {
                paths = JsonUtils.asJson(metricStore.getMetricPaths());
                contentType = "application/json";
            } else {
                paths = metricStore.getMetricPathsAsPlainStr();
                contentType = "text/plain";
            }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getHeaders().add(HttpHeader.CONTENT_TYPE,contentType);
                String stringPayload = paths ;
                response.write(true, ByteBuffer.wrap(stringPayload.getBytes()), callback);

        } else if (uri.startsWith("/api/metric-data")) {
          String metricPath = Request.getParameters(request).getValue("metricPath").toString();
          String countStr = Request.getParameters(request).getValue("count").toString();
          int count = 10;
            if (countStr != null && !countStr.trim().isEmpty()) {
                count = Integer.parseInt(countStr);
            }
            List<WorkbenchMetricStore.MetricData> metricData = null ;
            if(metricPath != null) {
                metricData = metricStore.getMetricData(metricPath) ;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getHeaders().add(HttpHeader.CONTENT_TYPE,"application/json");
            response.write(true, ByteBuffer.wrap(JsonUtils.asJson(metricData).getBytes()), callback);
        } else if(uri.startsWith("/api/stats")) {
            String content;
            String contentType;
            if (isContentTypeJson(request)) {
                content = JsonUtils.asJson(metricStore.getStats());
                contentType = "application/json";
            } else {
                content = metricStore.getStatsAsStr();
                contentType = "text/plain";
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getHeaders().add(HttpHeader.CONTENT_TYPE,contentType);
            response.write(true, ByteBuffer.wrap(content.getBytes()), callback);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getHeaders().add(HttpHeader.CONTENT_TYPE,"application/json");
            response.write(true, ByteBuffer.wrap("".getBytes()), callback);
        }
        callback.succeeded();
        return true;
    }

    private boolean isContentTypeJson(Request request) {
        String accept = request.getHeaders().get("accept");
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

    public static void main(String[] args) throws IOException, ClassNotFoundException, Exception {
        File extensionDir = resolveDirectory(WorkBenchServer.class);
        if (extensionDir != null) {
            logger = ExtensionsLoggerFactory.getLogger(WorkBenchServer.class);
            bootstrap(args, extensionDir);
        } else {
            System.out.println("[ERROR] Cannot resolve the install Directory");
        }
    }

    public static void bootstrap(String[] args, File extensionDir) throws IOException,Exception {
        final Monitor monitor = Monitor.from(extensionDir);
        final Map<String, String> taskArgs = getTaskArgs(monitor);
        final Object implClass = getImplClassInstance(monitor);
        if (implClass != null) {
            System.setProperty(WORKBENCH_MODE_PROPERTY, "true");
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
            workBenchServer.Start();
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
