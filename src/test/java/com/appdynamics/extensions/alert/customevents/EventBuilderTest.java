package com.appdynamics.extensions.alert.customevents;


import com.appdynamics.extensions.alerts.customevents.Event;
import com.appdynamics.extensions.alerts.customevents.EventBuilder;
import com.appdynamics.extensions.alerts.customevents.HealthRuleViolationEvent;
import com.appdynamics.extensions.alerts.customevents.OtherEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

public class EventBuilderTest {

    EventBuilder eventBuilder = new EventBuilder();
    EventArgs eventArgs = new EventArgs();

    @Test
    public void buildHealthRuleViolationEventWithOneEvalEntityAndTriggerNoBaseline() throws FileNotFoundException {
        String[] args = eventArgs.getHealthRuleViolationEventWithOneEvalEntityAndTriggerNoBaseline();
        Event event = eventBuilder.build(args);
        Assert.assertTrue(event != null);
        Assert.assertTrue(event instanceof HealthRuleViolationEvent);
    }


    @Test
    public void buildHealthRuleViolationEventWithMultipleEvalEntityAndATriggerNoBaseline() throws FileNotFoundException {
        String[] args = eventArgs.getHealthRuleViolationEventWithMultipleEvalEntityAndATriggerNoBaseline();
        Event event = eventBuilder.build(args);
        Assert.assertTrue(event != null);
        Assert.assertTrue(event instanceof HealthRuleViolationEvent);
        Assert.assertTrue(((HealthRuleViolationEvent) event).getEvaluationEntity().size() == 2);
        Assert.assertTrue(((HealthRuleViolationEvent) event).getEvaluationEntity().get(0).getTriggeredConditions().size() == 1);
        Assert.assertTrue(event.getAccountId() != null);
        Assert.assertTrue(event.getAccountName() != null);
    }

    @Test
    public void buildHealthRuleViolationEventWithMultipleEvalEntityAndMultipleTriggerNoBaseline() throws FileNotFoundException {
        String[] args = eventArgs.getHealthRuleViolationEventWithMultipleEvalEntityAndMultipleTriggerBaseline();
        Event event = eventBuilder.build(args);
        Assert.assertTrue(event != null);
        Assert.assertTrue(event instanceof HealthRuleViolationEvent);
        Assert.assertTrue(((HealthRuleViolationEvent) event).getEvaluationEntity().size() == 2);
        Assert.assertTrue(((HealthRuleViolationEvent) event).getEvaluationEntity().get(0).getTriggeredConditions().size() == 2);
        Assert.assertTrue(((HealthRuleViolationEvent) event).getEvaluationEntity().get(1).getTriggeredConditions().size() == 1);
        Assert.assertTrue(event.getAccountId() != null);
        Assert.assertTrue(event.getAccountName() != null);
    }


    @Test
    public void buildHealthRuleViolationEventWithMultipleEvalEntityAndMultipleTriggerBaseline() throws FileNotFoundException {
        String[] args = eventArgs.getHealthRuleViolationEventWithMultipleEvalEntityAndMultipleTriggerBaseline();
        Event event = eventBuilder.build(args);
        Assert.assertTrue(event != null);
        Assert.assertTrue(event instanceof HealthRuleViolationEvent);
        Assert.assertTrue(((HealthRuleViolationEvent) event).getEvaluationEntity().size() == 2);
        Assert.assertTrue(((HealthRuleViolationEvent) event).getEvaluationEntity().get(0).getTriggeredConditions().get(0).getBaselineName() != null);
        Assert.assertTrue(((HealthRuleViolationEvent) event).getEvaluationEntity().get(0).getTriggeredConditions().get(1).getBaselineName() == null);
        Assert.assertTrue(event.getAccountId() != null);
        Assert.assertTrue(event.getAccountName() != null);
    }



    @Test
    public void buildOtherEvent() throws FileNotFoundException {
        String[] args = eventArgs.getOtherEvent();
        Event event = eventBuilder.build(args);
        Assert.assertTrue(event != null);
        Assert.assertTrue(event instanceof OtherEvent);
        Assert.assertTrue(((OtherEvent) event).getEventTypes().size() == 2);
        Assert.assertTrue(((OtherEvent) event).getEventSummaries().size() == 2);
        Assert.assertTrue(event.getAccountId() != null);
        Assert.assertTrue(event.getAccountName() != null);
    }

}
