/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.conf.processor;

import com.appdynamics.extensions.asserts.CustomAsserts;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Satish Muddam
 */
@RunWith(PowerMockRunner.class)
public class ConfigProcessorTest {

    private static final String CONFIG_FILE = "src/test/resources/conf/config_SystemEnvVariables.yml";

    private static final String CONIG_EC2 = "src/test/resources/conf/config_ec2_SystemVariables.yml";

    private Map<String, String> populateSystemEnvParams() {
        Map<String, String> dummy = new HashMap<>();
        dummy.put("TEST_SERVER1", "TestServer1");
        dummy.put("TEST_HOST1", "localhost");
        dummy.put("TEST_PORT1", "8080");
        dummy.put("TEST_PASSWORD1", "admin");
        return dummy;
    }

    private Map<String, String> populateSystemEnvParamsForEC2() {
        Map<String, String> dummy = new HashMap<>();
        dummy.put("AWS_ACCESS_KEY1", "MyEC2AccessKey1");
        dummy.put("AWS_SECRET_KEY1", "MyEC2SecretKey1");
        dummy.put("AWS_ACCESS_KEY2", "MyEC2AccessKey2");
        dummy.put("AWS_SECRET_KEY2", "MyEC2SecretKey2");
        dummy.put("PROXY_HOST", "localhost");
        dummy.put("PROXY_PORT", "8090");
        dummy.put("PROXY_USER", "admin");
        dummy.put("PROXY_PASSWORD", "admin");
        return dummy;
    }

    @Test
    @PrepareForTest(ConfigProcessor.class)
    public void testConfigEnvironmentVariables() {
        PowerMockito.mockStatic(System.class);
        Mockito.when(System.getenv()).thenReturn(populateSystemEnvParams());
        Map<String, ?> rootElem = YmlReader.readFromFileAsMap(new File(CONFIG_FILE));
        if (rootElem == null) {
            Assert.fail("Unable to get data from the config file");
        }
        Map<String, ?> process = ConfigProcessor.process(rootElem);
        List<Map<String, String>> serverConfigs = (List<Map<String, String>>) process.get("servers");
        Assert.assertEquals(1, serverConfigs.size());
        Map<String, String> server1Config = serverConfigs.get(0);
        Assert.assertEquals("TestServer1", server1Config.get("name"));
        Assert.assertEquals("localhost", server1Config.get("host"));
        Assert.assertEquals("8080", server1Config.get("port"));
        Assert.assertEquals("admin", server1Config.get("password"));
    }

    @Test
    @PrepareForTest(ConfigProcessor.class)
    public void testConfigEnvironmentVariablesForEC2() {
        PowerMockito.mockStatic(System.class);
        Mockito.when(System.getenv()).thenReturn(populateSystemEnvParamsForEC2());
        Map<String, ?> rootElem = YmlReader.readFromFileAsMap(new File(CONIG_EC2));
        if (rootElem == null) {
            Assert.fail("Unable to get data from the config file");
        }
        Map<String, ?> awsConfig = ConfigProcessor.process(rootElem);
        List<Map<String, String>> awsAccounts = (List<Map<String, String>>) awsConfig.get("accounts");
        Assert.assertEquals(2, awsAccounts.size());
        Map<String, String> awsAccount1 = awsAccounts.get(0);
        Map<String, String> awsAccount2 = awsAccounts.get(0);
        //Different asserts, getting values from different object
        CustomAsserts.assertOneOf(Lists.newArrayList("MyEC2AccessKey1", "MyEC2AccessKey2"), awsAccount1.get("awsAccessKey"));
        CustomAsserts.assertOneOf(Lists.newArrayList("MyEC2AccessKey1", "MyEC2AccessKey2"), awsAccount2.get("awsAccessKey"));
        CustomAsserts.assertOneOf(Lists.newArrayList("MyEC2SecretKey1", "MyEC2SecretKey2"), awsAccount1.get("awsSecretKey"));
        CustomAsserts.assertOneOf(Lists.newArrayList("MyEC2SecretKey1", "MyEC2SecretKey2"), awsAccount2.get("awsSecretKey"));
        Map<String, ?> proxyConfig = (Map<String, ?>) awsConfig.get("proxyConfig");
        Assert.assertEquals("localhost", proxyConfig.get("host"));
        Assert.assertEquals("8090", proxyConfig.get("port"));
        Assert.assertEquals("admin", proxyConfig.get("username"));
        Assert.assertEquals("admin", proxyConfig.get("password"));
    }
}