package com.appdynamics.extensions.util.derived;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class OperandsProcessorTest {
    private OperandsProcessor operandsProcessor;

    @Before
    public void init(){
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|Queue|{y}|hits");
        operands.add("{x}|Queue|{y}|misses");
        operandsProcessor = new OperandsProcessor(operands);
    }

    @Test
    public void checkForFirstVariableTest(){
        String firstVariable = operandsProcessor.checkForFirstVariable();
        Assert.assertTrue(firstVariable.equals("{x}"));
    }

    @Test
    public void getModifiedOperandsTest(){
        Set<String> modifiedOperands = operandsProcessor.getModifiedOperands("{x}", "Server1");
        Assert.assertTrue(modifiedOperands.contains("Server1|Queue|{y}|hits"));
        Assert.assertTrue(modifiedOperands.contains("Server1|Queue|{y}|misses"));
        Assert.assertFalse(modifiedOperands.contains("{x}|Queue|{y}|hits"));
        Assert.assertFalse(modifiedOperands.contains("{x}|Queue|{y}|misses"));

    }

    @Test
    public void replacePathTest(){
        String path = operandsProcessor.getModifiedPath("{x}|Queue|{y}|hits","{x}", "Server1");
        Assert.assertTrue(path.equals("Server1|Queue|{y}|hits"));
    }

    @Test
    public void getMoidifiedExpressionTest(){
        Set<String> modifiedOperands = Sets.newHashSet();
        modifiedOperands.add("Server1|Queue|Q1|hits");
        modifiedOperands.add("Server1|Queue|Q1|misses");
        String expression = "{x}|Queue|{y}|hits / ({x}|Queue|{y}|hits + {x}|Queue|{y}|misses)";
        String modifiedExpression = operandsProcessor.getModifiedExpression(modifiedOperands, expression);
        Assert.assertTrue(modifiedExpression.equals("Server1|Queue|Q1|hits / (Server1|Queue|Q1|hits + Server1|Queue|Q1|misses)"));
    }
}
