package com.appdynamics.extensions.http;

import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.Collections;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/8/14
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleHttpClientBuilder {
    public static final Logger logger = LoggerFactory.getLogger(SimpleHttpClientBuilder.class);
    private Map<String, String> taskArgs;
    private JAXBContext jaxbContext;
    private HttpConnectionManagerParams params;
    private boolean multiThreaded;

    public SimpleHttpClientBuilder(Map<String, String> taskArgs) {
        this.taskArgs = taskArgs;
    }

    public SimpleHttpClientBuilder() {
        this.taskArgs = Collections.emptyMap();
    }

    public SimpleHttpClientBuilder jaxbClasses(Class... classes) {
        try {
            jaxbContext = JAXBContext.newInstance(classes);
        } catch (JAXBException e) {
            logger.error("", e);
        }
        return this;
    }

    public SimpleHttpClientBuilder connectionManagerParams(HttpConnectionManagerParams params) {
        this.params = params;
        return this;
    }

    public SimpleHttpClientBuilder socketTimeout(int timeout) {
        if (params == null) {
            params = new HttpConnectionManagerParams();
        }
        params.setSoTimeout(timeout);
        return this;
    }

    public SimpleHttpClientBuilder connectionTimeout(int timeout) {
        if (params == null) {
            params = new HttpConnectionManagerParams();
        }
        params.setConnectionTimeout(timeout);
        return this;
    }

    public SimpleHttpClientBuilder multiThreaded(){
        this.multiThreaded = true;
        return this;
    }


    public SimpleHttpClient build() {
        return new SimpleHttpClient(taskArgs, jaxbContext, params,multiThreaded);
    }


}
