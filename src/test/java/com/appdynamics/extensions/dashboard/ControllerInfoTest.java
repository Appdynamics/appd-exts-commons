package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * Created by abey.tom on 2/12/16.
 */
public class ControllerInfoTest {

    @Test
    public void testFromXml() throws Exception {
        File file = new File("src/test/resources/dashboard/test-controller-info.xml");
        ControllerInfo info = ControllerInfo.fromXml(file);
        info.setUsername("user");
        validate(info);
    }

    private void validate(ControllerInfo info) {
        Assert.assertEquals("host",info.getControllerHost());
        Assert.assertEquals(new Integer(80),info.getControllerPort());
        Assert.assertEquals(new Boolean(true),info.getControllerSslEnabled());
        Assert.assertEquals("account",info.getAccount());
        Assert.assertEquals("user",info.getUsername());
        Assert.assertEquals("welcome",info.getPassword());
        Assert.assertEquals("app",info.getApplicationName());
        Assert.assertEquals("tier",info.getTierName());
    }

    @Test
    public void testFromYml() throws Exception {
        Map config = YmlReader.readFromFile(new File("src/test/resources/dashboard/controller-info.yml"));
        ControllerInfo info = ControllerInfo.fromYml(config);
        validate(info);
    }

    @Test
    public void testFromSystemProperties() throws Exception {
        System.setProperty("appdynamics.agent.accountAccessKey","welcome");
        System.setProperty("appdynamics.agent.accountName","account");
        System.setProperty("appdynamics.agent.applicationName","app");
        System.setProperty("appdynamics.agent.tierName","tier");
        System.setProperty("appdynamics.controller.hostName","host");
        System.setProperty("appdynamics.controller.port","80");
        System.setProperty("appdynamics.controller.ssl.enabled","true");
        ControllerInfo info = ControllerInfo.fromSystemProperties();
        info.setUsername("user");
        validate(info);
    }

    @Test
    public void mergeTest(){
        Map config = YmlReader.readFromFile(new File("src/test/resources/dashboard/controller-info.yml"));
        ControllerInfo info1 = ControllerInfo.fromYml(config);
        ControllerInfo info2 = new ControllerInfo();
        info2.setControllerHost("host");
        info2.setControllerPort(81);
        info2.setControllerSslEnabled(false);
        info2.setAccount("account1");
        info2.setUsername("user1");
        info2.setPassword("welcome1");
        info2.setApplicationName("app1");
        info2.setTierName("tier1");
        ControllerInfo merge = info2.merge(info1);
        validate(merge);
    }
}