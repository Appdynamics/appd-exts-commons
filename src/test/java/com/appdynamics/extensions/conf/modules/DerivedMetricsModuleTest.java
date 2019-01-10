/*
 * Copyright (c) 2019 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.conf.modules;

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
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        DerivedMetricsModule derivedMetricsModule = new DerivedMetricsModule();
        DerivedMetricsCalculator derivedMetricsCalculator = derivedMetricsModule.initDerivedMetricsCalculator(conf, "Custom Metrics|Redis");
        Assert.assertTrue(derivedMetricsCalculator != null);
    }

    @Test
    public void derivedMetricsSectionNotPresentTest(){
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config_NoDerivedSection.yml"));
        DerivedMetricsModule derivedMetricsModule = new DerivedMetricsModule();
        DerivedMetricsCalculator derivedMetricsCalculator = derivedMetricsModule.initDerivedMetricsCalculator(conf, "Custom Metrics|Redis");
        Assert.assertTrue(derivedMetricsCalculator == null);
    }
}
