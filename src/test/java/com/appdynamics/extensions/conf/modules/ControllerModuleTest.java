package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * Created by venkata.konala on 1/3/19.
 */
public class ControllerModuleTest {

    @Test
    public void whenControllerInfoValidatedAllThreeComponentsAreNotNullTest() {
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/controller/config.yml"));
        ControllerModule controllerModule = new ControllerModule();
        controllerModule.initController(null, conf);
        Assert.assertNotNull(controllerModule.getControllerInfo());
        Assert.assertNotNull(controllerModule.getControllerClient());
        Assert.assertNotNull(controllerModule.getControllerAPIService());

    }

    @Test
    public void whenControllerInfoNotValidatedAllThreeComponentsAreNullTest() {
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config_withIncompleteControllerInfo"));
        ControllerModule controllerModule = new ControllerModule();
        controllerModule.initController(null, conf);
        Assert.assertNull(controllerModule.getControllerInfo());
        Assert.assertNull(controllerModule.getControllerClient());
        Assert.assertNull(controllerModule.getControllerAPIService());

    }
}
