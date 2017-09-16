package com.appdynamics.extensions.http;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.util.YmlUtils;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.appdynamics.extensions.TaskInputArgs.defaultIfEmpty;
import static com.appdynamics.extensions.util.AssertUtils.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/4/14
 * Time: 9:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class UrlBuilder {
    public static final Logger logger = LoggerFactory.getLogger(UrlBuilder.class);
    private List<String> paths;
    private Map<String, String> taskArgs;
    private Map<String, String> queryParams;

    public UrlBuilder() {
        taskArgs = new HashMap<String, String>();
    }

    public UrlBuilder(Map<String, String> taskArgs) {
        if (taskArgs == null) {
            throw new IllegalArgumentException("The task arguments to the URLBuilder cannot be null");
        }
        this.taskArgs = new HashMap<String, String>(taskArgs);
    }

    public static UrlBuilder builder() {
        return new UrlBuilder();
    }

    public static UrlBuilder builder(Map<String, String> taskArgs) {
        return new UrlBuilder(taskArgs);
    }



    public UrlBuilder ssl(boolean useSSL){
        taskArgs.put(TaskInputArgs.USE_SSL,String.valueOf(useSSL));
        return this;
    }

    public UrlBuilder host(String host){
        taskArgs.put(TaskInputArgs.HOST,host);
        return this;
    }

    public UrlBuilder port(String port){
        taskArgs.put(TaskInputArgs.PORT,port);
        return this;
    }

    public UrlBuilder port(int port){
        taskArgs.put(TaskInputArgs.PORT,String.valueOf(port));
        return this;
    }

    public UrlBuilder uri(String uri){
        taskArgs.put(TaskInputArgs.URI,uri);
        return this;
    }

    public UrlBuilder path(String path) {
        if (!Strings.isNullOrEmpty(path)) {
            path = path.trim();
            path = trimLeadingSlash(path);
            path = trimTrailingSlash(path);
            if (paths == null) {
                paths = new ArrayList<String>();
            }
            paths.add(path);
        }
        return this;
    }

    private String trimLeadingSlash(String path) {
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    private String trimTrailingSlash(String path) {
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public UrlBuilder query(String name, String value) {
        if (!Strings.isNullOrEmpty(name)) {
            name = name.trim();
            if (!!Strings.isNullOrEmpty(value)) {
                value = "";
            }
            if (queryParams == null) {
                queryParams = new LinkedHashMap<String, String>();
            }
            queryParams.put(name, value);
        }
        return this;
    }

    public UrlBuilder query(String name) {
        return query(name, null);
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        String uri = taskArgs.get(TaskInputArgs.URI);
        if(!Strings.isNullOrEmpty(uri)){
            sb.append(trimTrailingSlash(uri.trim()));
        }else{
            String useSSL = defaultIfEmpty(taskArgs, TaskInputArgs.USE_SSL, "false");
            if (Boolean.valueOf(useSSL)) {
                sb.append("https://");
            } else {
                sb.append("http://");
            }
            String host = defaultIfEmpty(taskArgs, TaskInputArgs.HOST, "localhost");
            String port = defaultIfEmpty(taskArgs, TaskInputArgs.PORT, "80");
            sb.append(host).append(":").append(port);
        }

        if (paths != null && !paths.isEmpty()) {
            for (String path : paths) {
                sb.append("/").append(path);
            }
        }
        if (queryParams != null && !queryParams.isEmpty()) {
            sb.append("?");
            Set<String> keys = queryParams.keySet();
            for (String key : keys) {
                sb.append(key);
                String value = queryParams.get(key);
                if (!value.isEmpty()) {
                    sb.append("=").append(value);
                }
                sb.append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        if(logger.isDebugEnabled()){
            logger.debug("The url is initialized to {}",sb);
        }
        return sb.toString();
    }

    public static UrlBuilder fromYmlServerConfig(Map server) {
        UrlBuilder builder = new UrlBuilder();
        String uri = (String) server.get(TaskInputArgs.URI);
        if (!Strings.isNullOrEmpty(uri)) {
            builder.uri(uri);
        } else {
            String host = (String) server.get(TaskInputArgs.HOST);
            assertNotNull(host, "Either [uri] or [host] should be set for each server");
            Integer port = YmlUtils.getInteger(server.get(TaskInputArgs.PORT));
            Boolean useSSL = YmlUtils.getBoolean(server.get("useSSL"));
            if (useSSL == null) {
                useSSL = false;
            }
            if (port == null) {
                if (useSSL) {
                    port = 443;
                } else {
                    port = 80;
                }
            }
            builder.host(host).port(port).ssl(useSSL);
        }
        return builder;
    }
}
