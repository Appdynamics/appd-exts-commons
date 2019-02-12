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

package com.appdynamics.extensions.metrics;

import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author pradeep.nair
 */

public class MetricCharSequenceReplacerTest {

    private MetricCharSequenceReplacer replacer;

    @Test
    public void whenNotConfiguredThenDefaultsShouldBeUsed() {
        Map<String, ?> conf = YmlReader.readFromFile(new File
                ("src/test/resources/metricReplace/config_no_metric_replace.yml"));
        replacer = MetricCharSequenceReplacer.createInstance(conf);
        assertChecks(conf);
    }

    @Test
    public void whenConfiguredThenReplacementsAndDefaultsShouldBeUsed() {
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/metricReplace/config.yml"));
        replacer = MetricCharSequenceReplacer.createInstance(conf);
        assertChecks(conf);
    }

    @Test
    public void whenConfiguredToOverrideDelimitersThenDefaultShouldNotBeUsed() {
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/metricReplace/config_with_overrides" +
                ".yml"));
        replacer = MetricCharSequenceReplacer.createInstance(conf);
        assertChecks(conf);
    }

    @Test
    public void whenReplaceWithIsWrongThenEmptyStringIsUsed() {
        Map<String, ?> conf = YmlReader.readFromFile(new File
                ("src/test/resources/metricReplace/config_with_wrong_replacement.yml"));
        replacer = MetricCharSequenceReplacer.createInstance(conf);
        assertChecks(conf);
    }

    @Test
    public void whenDefaultCharsetIsUTF8ThenReplacementForUnicodeCharactersShouldWork() {
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/metricReplace/config_with_non_ascii" +
                ".yml"));
        replacer = MetricCharSequenceReplacer.createInstance(conf);
        assertChecks(conf);
    }

    @Test
    public void whenUnicodeLiteralsAreUsedThenReplacementShouldWork() {
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/metricReplace/config_with_non_ascii" +
                ".yml"));
        replacer = MetricCharSequenceReplacer.createInstance(conf);
        assertChecks(conf);
    }

    @Test
    public void whenMultipleReplacementsAreConfiguredForSameCharacterThenLastOneIsUsed() {
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/metricReplace/config_multiple.yml"));
        replacer = MetricCharSequenceReplacer.createInstance(conf);
        assertChecks(conf);
    }

    private void assertChecks(Map<String, ?> conf) {
        assertNotNull(replacer);
        Map<String, String> replacedNames = new HashMap<>();
        for (String metricName : readMetricNames(conf)) {
            replacedNames.put(metricName, replacer.getReplacementFromCache(metricName));
        }
        Map<String, String> finalMetricName = readFinalNames(conf);
        assertEquals(finalMetricName, replacedNames);
        assertHasNoDelimiter(new ArrayList<>(replacedNames.values()));
    }

    private List<String> readMetricNames(Map<String, ?> conf) {
        List<String> metricNames = new ArrayList<>();
        List<Map<String, ?>> metrics = (List<Map<String, ?>>) conf.get("metrics");
        for (Map<String, ?> map : metrics) {
            metricNames.add((String) map.keySet().toArray()[0]);
        }
        return metricNames;
    }

    private Map<String, String> readFinalNames(Map<String, ?> conf) {
        return (Map<String, String>) conf.get("finalMetricName");
    }

    private void assertHasNoDelimiter(List<String> replacedNames) {
        for (String replacedName : replacedNames) {
            assertFalse(replacedName.contains("|"));
            assertFalse(replacedName.contains(","));
            assertFalse(replacedName.contains(":"));
        }
    }
}
