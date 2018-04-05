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
public class ExtensionContextConfigurationTest {

    //private File installDir = PathResolver.resolveDirectory()
    private ExtensionContextConfiguration extensionContextConfiguration = new ExtensionContextConfiguration("Redis Monitor", "Custom Metrics|Redis", new File(""), null);

    @Test
    public void configYMLreturnMapAndIntializesContextWhenFilePathIsValidTest(){
        extensionContextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        Assert.assertTrue(extensionContextConfiguration.isEnabled());
        Assert.assertTrue(extensionContextConfiguration.getConfigYml() != null);
        Assert.assertTrue(extensionContextConfiguration.getMetricPrefix().equals("Server|Component:AppLevels|Custom Metrics|Redis"));
        Assert.assertTrue(extensionContextConfiguration.getContext() != null);
        Assert.assertFalse(extensionContextConfiguration.getContext().isScheduledModeEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void configYMLreturnExceptionAndDoesNotInitialiseContextWhenFilePathIsNotValidTest(){
        extensionContextConfiguration.setConfigYml("config.yml");
        Assert.assertTrue(extensionContextConfiguration.getContext() == null);
    }
}
