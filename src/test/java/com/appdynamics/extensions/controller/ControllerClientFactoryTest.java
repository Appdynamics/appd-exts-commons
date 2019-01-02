package com.appdynamics.extensions.controller;

import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.yml.YmlReader;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * Created by venkata.konala on 1/2/19.
 */

public class ControllerClientFactoryTest {

    @Test
    public void whenSSLNotEnabledThenShouldUseHttp() {
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/controller/config.yml"));
        ControllerInfoFactory.initialize((Map<String, ?>)config.get("controllerInfo"), PathResolver.resolveDirectory(AManagedMonitor.class));
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo();
        ControllerClientFactory.initialize(controllerInfo, (Map<String, ?>)config.get("connection"), (Map<String, ?>)config.get("proxy"),(String)config.get("encryptionKey"));
        ControllerClient controllerClient = ControllerClientFactory.getControllerClient();
        Assert.assertNotNull(controllerClient.getBaseURL());
        Assert.assertNotNull(controllerClient.getHttpClient());
        Assert.assertEquals(controllerClient.getBaseURL(), "http://localhost:8090/");

    }

    @Test
    public void whenSSLEnabledThenShouldUseHttps() {
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/controller/config.yml"));
        ControllerInfoFactory.initialize((Map<String, ?>)config.get("controllerInfo"), PathResolver.resolveDirectory(AManagedMonitor.class));
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo();
        controllerInfo.setControllerSslEnabled(true);
        ControllerClientFactory.initialize(controllerInfo, (Map<String, ?>)config.get("connection"), (Map<String, ?>)config.get("proxy"),(String)config.get("encryptionKey"));
        ControllerClient controllerClient = ControllerClientFactory.getControllerClient();
        Assert.assertNotNull(controllerClient.getBaseURL());
        Assert.assertNotNull(controllerClient.getHttpClient());
        Assert.assertEquals(controllerClient.getBaseURL(), "https://localhost:8090/");

    }
}
