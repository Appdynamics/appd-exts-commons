/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.conf;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by venkata.konala on 4/2/18.
 */
public class MonitorContextConfigurationTest {

    //private File installDir = PathResolver.resolveDirectory()
    private MonitorContextConfiguration monitorContextConfiguration = new MonitorContextConfiguration("Redis Monitor", "Custom Metrics|Redis", new File(""), null);

    @Test
    public void configYMLreturnMapAndIntializesContextWhenFilePathIsValidTest(){
        monitorContextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        Assert.assertTrue(monitorContextConfiguration.isEnabled());
        Assert.assertTrue(monitorContextConfiguration.getConfigYml() != null);
        Assert.assertTrue(monitorContextConfiguration.getMetricPrefix().equals("Server|Component:AppLevels|Custom Metrics|Redis"));
        Assert.assertTrue(monitorContextConfiguration.getContext() != null);
        Assert.assertFalse(monitorContextConfiguration.getContext().isScheduledModeEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void configYMLreturnExceptionAndDoesNotInitialiseContextWhenFilePathIsNotValidTest(){
        monitorContextConfiguration.setConfigYml("config.yml");
        Assert.assertTrue(monitorContextConfiguration.getContext() == null);
    }
}
