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


import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.StringUtils;
import org.slf4j.Logger;

/**
 * Builds an event from command line arguments.
 */

public class EventBuilder {

    Logger logger = ExtensionsLoggerFactory.getLogger(EventBuilder.class);

    /**
     * Builds an event from command line arguments
     *
     * @param args
     * @return Event
     */
    public Event build(String[] args) {
        if (isEventValid(args)) {
            String[] cleanedArgs = cleanArgs(args);
            //confirmed with Jon Reid from controller team to determine the differentiating condition
            if (isHRVEvent(cleanedArgs[(cleanedArgs.length - 3)])) {
                HealthRuleViolationEvent event = createHealthRuleViolationEvent(cleanedArgs);
                return event;
            } else {
                OtherEvent otherEvent = createOtherEvent(cleanedArgs);
                return otherEvent;
            }
        }
        logger.error("Event is not valid. Args passed are ::" + args);
        return null;
    }

    private boolean isHRVEvent(String eventType) {
        if (eventType != null) {
            return eventType.startsWith("POLICY");
        }
        return false;
    }

    private OtherEvent createOtherEvent(String[] cleanedArgs) {
        OtherEvent otherEvent = new OtherEvent();
        setBasicEvent(cleanedArgs, otherEvent);
        otherEvent.setEventNotificationTime(cleanedArgs[2]);
        otherEvent.setEventNotificationName(cleanedArgs[6]);
        otherEvent.setEventNotificationId(cleanedArgs[7]);
        otherEvent.setEventNotificationIntervalInMin(cleanedArgs[8]);
        int currentArgPos = setOtherEventDetails(otherEvent, cleanedArgs);
        currentArgPos++;
        otherEvent.setDeepLinkUrl(cleanedArgs[currentArgPos]);
        currentArgPos++;
        otherEvent.setAccountName(cleanedArgs[currentArgPos]);
        currentArgPos++;
        otherEvent.setAccountId(cleanedArgs[currentArgPos]);
        return otherEvent;
    }

    private int setOtherEventDetails(OtherEvent otherEvent, String[] cleanedArgs) {
        int currentArgPos = 9;
        try {
            int numOfEventTypes = Integer.parseInt(cleanedArgs[currentArgPos]);
            otherEvent.setNumberOfEventTypes(cleanedArgs[currentArgPos]);
            for (int index = 1; index <= numOfEventTypes; index++) {
                EventType eventType = new EventType();
                currentArgPos++;
                eventType.setEventType(cleanedArgs[currentArgPos]);
                currentArgPos++;
                eventType.setEventTypeNum(cleanedArgs[currentArgPos]);
                otherEvent.getEventTypes().add(eventType);
            }
            currentArgPos = setEventSummary(otherEvent, cleanedArgs, currentArgPos);
        } catch (NumberFormatException nfe) {
            logger.error("Cannot convert string to int because of mismatch of arguments ", nfe);
        }
        return currentArgPos;
    }

    private int setEventSummary(OtherEvent otherEvent, String[] cleanedArgs, int currentArgPos) {
        currentArgPos++;
        try {
            int numberOfEventSummaries = Integer.parseInt(cleanedArgs[currentArgPos]);
            otherEvent.setNumberOfEventSummaries(cleanedArgs[currentArgPos]);
            for (int index = 1; index <= numberOfEventSummaries; index++) {
                EventSummary eventSummary = new EventSummary();
                currentArgPos++;
                eventSummary.setEventSummaryId(cleanedArgs[currentArgPos]);
                currentArgPos++;
                eventSummary.setEventSummaryTime(cleanedArgs[currentArgPos]);
                currentArgPos++;
                eventSummary.setEventSummaryType(cleanedArgs[currentArgPos]);
                currentArgPos++;
                eventSummary.setEventSummarySeverity(cleanedArgs[currentArgPos]);
                currentArgPos++;
                eventSummary.setEventSummaryString(cleanedArgs[currentArgPos]);
                otherEvent.getEventSummaries().add(eventSummary);
            }
        } catch (NumberFormatException nfe) {
            logger.error("Cannot convert string to int because of mismatch of arguments ", nfe);
        }
        return currentArgPos;
    }

    private boolean isEventValid(String[] args) {
        //TODO kunal.gupta check if this condition makes sense
        if (args != null && args.length > 16) {
            return true;
        }
        return false;
    }

    private HealthRuleViolationEvent createHealthRuleViolationEvent(String[] cleanedArgs) {
        HealthRuleViolationEvent event = new HealthRuleViolationEvent();
        setBasicEvent(cleanedArgs, event);
        event.setPvnAlertTime(cleanedArgs[2]);
        event.setHealthRuleName(cleanedArgs[6]);
        event.setHealthRuleID(cleanedArgs[7]);
        event.setPvnTimePeriodInMinutes(cleanedArgs[8]);
        event.setAffectedEntityType(cleanedArgs[9]);
        event.setAffectedEntityName(cleanedArgs[10]);
        event.setAffectedEntityID(cleanedArgs[11]);
        int currentArgPos = setEvaluationDetails(event, cleanedArgs);
        currentArgPos++;
        event.setSummaryMessage(cleanedArgs[currentArgPos]);
        currentArgPos++;
        event.setIncidentID(cleanedArgs[currentArgPos]);
        currentArgPos++;
        event.setDeepLinkUrl(cleanedArgs[currentArgPos]);
        currentArgPos++;
        event.setEventType(cleanedArgs[currentArgPos]);
        event.setIncidentUrl(event.getDeepLinkUrl() + event.getIncidentID());
        currentArgPos++;
        event.setAccountName(cleanedArgs[currentArgPos]);
        currentArgPos++;
        event.setAccountId(cleanedArgs[currentArgPos]);
        return event;
    }


    private void setBasicEvent(String[] cleanedArgs, Event event) {
        event.setAppName(cleanedArgs[0]);
        event.setAppID(cleanedArgs[1]);
        event.setPriority(cleanedArgs[3]);
        event.setSeverity(cleanedArgs[4]);
        event.setTag(cleanedArgs[5]);
    }

    private int setEvaluationDetails(HealthRuleViolationEvent event, String[] cleanedArgs) {
        int currentArgPos = 12;
        try {
            int numOfEvaluationEntities = Integer.parseInt(cleanedArgs[12]);
            for (int index = 1; index <= numOfEvaluationEntities; index++) {
                EvaluationEntity eval = new EvaluationEntity();
                currentArgPos++;
                eval.setType(cleanedArgs[currentArgPos]);
                currentArgPos++;
                eval.setName(cleanedArgs[currentArgPos]);
                currentArgPos++;
                eval.setId(cleanedArgs[currentArgPos]);
                currentArgPos = setTriggeredConditionDetails(eval, cleanedArgs, currentArgPos);
                event.getEvaluationEntity().add(eval);
            }
        } catch (NumberFormatException nfe) {
            logger.error("Cannot convert string to int because of mismatch of arguments ", nfe);
        }
        return currentArgPos;
    }

    private int setTriggeredConditionDetails(EvaluationEntity eval, String[] cleanedArgs, int currentArgPos) {
        try {
            currentArgPos++;
            int numOfTriggeredCond = Integer.parseInt(cleanedArgs[currentArgPos]);
            eval.setNumberOfTriggeredConditions(cleanedArgs[currentArgPos]);
            for (int index = 1; index <= numOfTriggeredCond; index++) {
                TriggerCondition triggerCond = new TriggerCondition();
                currentArgPos++;
                triggerCond.setScopeType(cleanedArgs[currentArgPos]);
                currentArgPos++;
                triggerCond.setScopeName(cleanedArgs[currentArgPos]);
                currentArgPos++;
                triggerCond.setScopeId(cleanedArgs[currentArgPos]);
                currentArgPos++;
                triggerCond.setConditionName(cleanedArgs[currentArgPos]);
                currentArgPos++;
                triggerCond.setConditionId(cleanedArgs[currentArgPos]);
                currentArgPos++;
                triggerCond.setOperator(getOperator(cleanedArgs[currentArgPos]));
                currentArgPos++;
                triggerCond.setConditionUnitType(cleanedArgs[currentArgPos]);
                if (triggerCond.getConditionUnitType() != null && triggerCond.getConditionUnitType().toUpperCase().startsWith("BASELINE")) {
                    currentArgPos = setBaseLineDetails(triggerCond, cleanedArgs, currentArgPos);
                }
                currentArgPos++;
                triggerCond.setThresholdValue(cleanedArgs[currentArgPos]);
                currentArgPos++;
                triggerCond.setObservedValue(cleanedArgs[currentArgPos]);
                eval.getTriggeredConditions().add(triggerCond);
            }
        } catch (NumberFormatException nfe) {
            logger.error("Cannot convert string to int because of mismatch of arguments", nfe);
        }
        return currentArgPos;
    }

    private String getOperator(String operator) {
        if ("LESS_THAN".equalsIgnoreCase(operator)) {
            return "<";
        }
        if ("LESS_THAN_EQUALS".equalsIgnoreCase(operator)) {
            return "<=";
        }
        if ("GREATER_THAN".equalsIgnoreCase(operator)) {
            return ">";
        }
        if ("GREATER_THAN_EQUALS".equalsIgnoreCase(operator)) {
            return ">=";
        }
        if ("EQUALS".equalsIgnoreCase(operator)) {
            return "==";
        }
        if ("NOT_EQUALS".equalsIgnoreCase(operator)) {
            return "!=";
        }
        return "";
    }

    private int setBaseLineDetails(TriggerCondition triggerCond, String[] cleanedArgs, int currentArgPos) {
        currentArgPos++;
        triggerCond.setUseDefaultBaseline(Boolean.valueOf(cleanedArgs[currentArgPos]));
        if (!triggerCond.isUseDefaultBaseline()) {
            currentArgPos++;
            triggerCond.setBaselineName(cleanedArgs[currentArgPos]);
            currentArgPos++;
            triggerCond.setBaselineId(cleanedArgs[currentArgPos]);
        }
        return currentArgPos;
    }


    private String[] cleanArgs(String[] args) {
        StringBuilder sb = new StringBuilder();
        String[] stripped = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            sb.append("args[" + i + "]=" + args[i] + ", ");
            stripped[i] = StringUtils.stripQuote(args[i]);
        }
        logger.debug(sb.toString());
        return stripped;
    }


}
