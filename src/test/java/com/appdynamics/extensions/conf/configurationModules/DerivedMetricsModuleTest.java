package com.appdynamics.extensions.conf.configurationModules;

import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class DerivedMetricsModuleTest {

    @Test
    public void derivedMetricsSectionPresentTest(){
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/configuration/config.yml"));
        DerivedMetricsModule derivedMetricsModule = new DerivedMetricsModule();
        DerivedMetricsCalculator derivedMetricsCalculator = derivedMetricsModule.initDerivedMetricsCalculator(conf, "Custom Metrics|Redis");
        Assert.assertTrue(derivedMetricsCalculator != null);
    }

    @Test
    public void derivedMetricsSectionNotPresentTest(){
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/configuration/config_NoDerivedSection.yml"));
        DerivedMetricsModule derivedMetricsModule = new DerivedMetricsModule();
        DerivedMetricsCalculator derivedMetricsCalculator = derivedMetricsModule.initDerivedMetricsCalculator(conf, "Custom Metrics|Redis");
        Assert.assertTrue(derivedMetricsCalculator == null);
    }
}
