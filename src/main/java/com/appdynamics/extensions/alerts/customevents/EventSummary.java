/*
 * Copyright (c) 2018 AppDynamics,Inc.
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


public class EventSummary {

    private String eventSummaryId;
    private String eventSummaryTime;
    private String eventSummaryType;
    private String eventSummarySeverity;
    private String eventSummaryString;

    public String getEventSummaryId() {
        return eventSummaryId;
    }

    public void setEventSummaryId(String eventSummaryId) {
        this.eventSummaryId = eventSummaryId;
    }

    public String getEventSummaryTime() {
        return eventSummaryTime;
    }

    public void setEventSummaryTime(String eventSummaryTime) {
        this.eventSummaryTime = eventSummaryTime;
    }

    public String getEventSummaryType() {
        return eventSummaryType;
    }

    public void setEventSummaryType(String eventSummaryType) {
        this.eventSummaryType = eventSummaryType;
    }

    public String getEventSummarySeverity() {
        return eventSummarySeverity;
    }

    public void setEventSummarySeverity(String eventSummarySeverity) {
        this.eventSummarySeverity = eventSummarySeverity;
    }

    public String getEventSummaryString() {
        return eventSummaryString;
    }

    public void setEventSummaryString(String eventSummaryString) {
        this.eventSummaryString = eventSummaryString;
    }
}
