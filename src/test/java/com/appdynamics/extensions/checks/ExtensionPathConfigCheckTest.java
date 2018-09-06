package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.conf.controller.ControllerInfo;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Satish Muddam
 */
public class ExtensionPathConfigCheckTest {

    private Logger logger = Mockito.mock(Logger.class);


    @Test
    public void testNullControllerInfo() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck(null, null, null, logger);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertEquals(value, "Received ControllerInfo as null. Not checking anything.");
    }

    @Test
    public void testNullMetricPrefix() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);


        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck(controllerInfo, new HashMap<String, String>(), null, logger);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertEquals(value, "Metric prefix not configured in config file");
    }

    @Test
    public void testMetricPrefixWithComponentAndSIMEnabled() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(true);

        HashMap<String, String> config = new HashMap<>();
        config.put("metricPrefix", "Server|Component:Test|Custom Metrics|Test");
        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck(controllerInfo, config, null, logger);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertEquals(value, "No need to configure tier-id as SIM is enabled. Please use the alternate metric prefix.");
    }

    @Test
    public void testMetricPrefixSIMNotEnabled() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);

        HashMap<String, String> config = new HashMap<>();
        config.put("metricPrefix", "Custom Metrics|Test");
        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck(controllerInfo, config, null, logger);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertEquals(value, "Configured metric prefix with no tier id. With this configuration, metric browser will show metric names in all the available tiers");
    }

    @Test
    public void testMetricPrefixSIMEnabled() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(true);

        HashMap<String, String> config = new HashMap<>();
        config.put("metricPrefix", "Custom Metrics|Test");
        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck(controllerInfo, config, null, logger);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(2)).info(logCaptor.capture());

        String value = logCaptor.getAllValues().get(1);
        Assert.assertEquals(value, "SIM is enabled, please look in the SIM metric browser for metrics.");
    }

    @Test
    public void testMetricPrefixWithComponentAndSIMNotEnabled() throws IOException {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("TestApp");
        Mockito.when(controllerInfo.getTierName()).thenReturn("TestTier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("TestNode");

        ControllerRequestHandler controllerRequestHandler = Mockito.mock(ControllerRequestHandler.class);
        Mockito.when(controllerRequestHandler.sendGet(Matchers.anyString())).thenReturn(tierRESTResponse());

        HashMap<String, String> config = new HashMap<>();
        config.put("metricPrefix", "Server|Component:TestTier|Custom Metrics|Test");
        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck(controllerInfo, config, controllerRequestHandler, logger);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(2)).info(logCaptor.capture());

        String value = logCaptor.getAllValues().get(1);
        Assert.assertEquals(value, "Extension configured correct tier id/tier name");
    }

    @Test
    public void testMetricPrefixWithWrongComponentAndSIMNotEnabled() throws IOException {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("TestApp");
        Mockito.when(controllerInfo.getTierName()).thenReturn("TestTier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("TestNode");

        ControllerRequestHandler controllerRequestHandler = Mockito.mock(ControllerRequestHandler.class);
        Mockito.when(controllerRequestHandler.sendGet(Matchers.anyString())).thenReturn(tierRESTResponse());

        HashMap<String, String> config = new HashMap<>();
        config.put("metricPrefix", "Server|Component:TestTier123|Custom Metrics|Test");
        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck(controllerInfo, config, controllerRequestHandler, logger);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertEquals(value, "Extension did not configure correct tier. Tier to configure [" + controllerInfo.getTierName() +
                "] with tier id [19], but configured tier [TestTier123]");
    }

    private String tierRESTResponse() {
        return "[{\"agentType\": \"APP_AGENT\", \"name\": \"mytier\", \"description\": \"\", \"id\": 19, \"numberOfNodes\": 1, \"type\": \"Application Server\" }]";
    }
}
