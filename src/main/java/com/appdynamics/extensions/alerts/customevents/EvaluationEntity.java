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

package com.appdynamics.extensions.alerts.customevents;


import java.util.ArrayList;
import java.util.List;

public class EvaluationEntity {

    private String type;
    private String name;
    private String id;
    private String numberOfTriggeredConditions;
    private List<TriggerCondition> triggeredConditions = new ArrayList<TriggerCondition>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumberOfTriggeredConditions() {
        return numberOfTriggeredConditions;
    }

    public void setNumberOfTriggeredConditions(String numberOfTriggeredConditions) {
        this.numberOfTriggeredConditions = numberOfTriggeredConditions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TriggerCondition> getTriggeredConditions() {
        return triggeredConditions;
    }

    public void setTriggeredConditions(List<TriggerCondition> triggeredConditions) {
        this.triggeredConditions = triggeredConditions;
    }
}
