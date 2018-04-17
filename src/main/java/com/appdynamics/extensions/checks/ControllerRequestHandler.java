/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.dashboard.ControllerInfo;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Satish Muddam
 */
public class ControllerRequestHandler {

    public Logger logger;

    private ControllerInfo controllerInfo;
    private CloseableHttpClient client;
    private String controllerURI;

    public ControllerRequestHandler(ControllerInfo controllerInfo, Logger logger) {
        this.logger = logger;
        this.controllerInfo = controllerInfo;
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(createHttpConfigMap());
        client = builder.build();
        getCookie();
    }

    private String loginCookie;

    private String getCookie() {
        if (loginCookie != null) {
            return loginCookie;
        }

        if (client == null) {
            logger.error("Can not get login cookie with null SimpleHttpClient");
            return null;
        }

        try {
            HttpGet get = new HttpGet(controllerURI + "/controller/auth?action=login");
            CloseableHttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                Header[] allHeaders = response.getAllHeaders();
                StringBuilder cookies = new StringBuilder();
                for (Header header : allHeaders) {
                    if (header.getName().equalsIgnoreCase("set-cookie")) {
                        String value = header.getValue();
                        cookies.append(value).append(";");
                    }
                }
                loginCookie = cookies.toString();
                //Releases the connection
                response.close();

            } else {
                logger.error("Not able to login to controller. Received status [{}] and response [{}]", response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity()));
            }
        } catch (Exception e) {
            logger.error("Exception while trying to get the MA status", e);
        }

        return loginCookie;
    }

    public String sendGet(String url) throws IOException {
        HttpGet get = new HttpGet(controllerURI + url);
        get.setHeader("Cookie", loginCookie);

        CloseableHttpResponse response = client.execute(get);
        String responseString = EntityUtils.toString(response.getEntity());
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            logger.debug("Response for url [{}] is [{}]", url, responseString);
            return responseString;
        }
        throw new InvalidResponseException(statusCode, responseString);
    }

    private Map<String, String> createHttpConfigMap() {
        Map map = new HashMap();

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();

        String controllerHost = controllerInfo.getControllerHost();
        String controllerPort = String.valueOf(controllerInfo.getControllerPort());
        Boolean sslEnabled = controllerInfo.getControllerSslEnabled();

        controllerURI = buildURI(controllerHost, controllerPort, sslEnabled);


        server.put("uri", controllerURI);
        String userName = getUserName(controllerInfo);
        if (userName != null && userName.length() > 0) {
            server.put("username", userName);
            server.put("password", CryptoUtil.encode(controllerInfo.getPassword()));
        }
        list.add(server);

        String proxyHost = System.getProperty("appdynamics.http.proxyHost");
        String proxyPort = System.getProperty("appdynamics.http.proxyPort");

        String proxyUser = System.getProperty("appdynamics.http.proxyUser");
        String proxyPasswordFilePath = System.getProperty("appdynamics.http.proxyPasswordFile");


        if (StringUtils.hasText(proxyHost) && StringUtils.hasText(proxyPort)) {

            HashMap<String, String> proxyProps = new HashMap<String, String>();
            map.put("proxy", proxyProps);

            logger.debug("Using the proxy {}:{} to communicate with the controller", proxyHost, proxyPort);
            proxyProps.put("uri", buildURI(proxyHost, proxyPort, false));

            if (StringUtils.hasText(proxyUser)) {
                proxyProps.put("username", proxyUser);
            }

            if (StringUtils.hasText(proxyPasswordFilePath)) {
                List<String> lines = null;
                try {
                    lines = FileUtils.readLines(new File(proxyPasswordFilePath));
                } catch (IOException e) {
                    logger.error("Unable to read the proxy password from the file");
                }
                if (lines != null && lines.size() > 0) {
                    proxyProps.put("password", lines.get(0));
                }
            }
        }
        return map;
    }

    private String buildURI(String host, String port, boolean sslEnabled) {
        StringBuilder sb = new StringBuilder();
        if (sslEnabled) {
            sb.append("https://");
        } else {
            sb.append("http://");
        }
        sb.append(host).append(":").append(port);
        return sb.toString();
    }


    private String getUserName(ControllerInfo controllerInfo) {
        String accountName = controllerInfo.getAccount();
        if (accountName != null) {
            return "singularity-agent@" + accountName;
        }
        return "";
    }
}