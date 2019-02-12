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

package com.appdynamics.extensions.metrics.derived;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class OperandsHandlerTest {
    private OperandsHandler operand;
    private DerivedMetricsPathHandler pathHandler;

    @Before
    public void init(){
        String formula = "{x}|Queue|{y}|hits / ({x}|Queue|{y}|hits + {x}|Queue|{y}|misses)";
        pathHandler = new DerivedMetricsPathHandler();
        operand = new OperandsHandler(formula, pathHandler);
    }

    @Test
    public void getOperandsFromFormulaTest(){
        Set<String> operands = operand.getBaseOperands();
        Assert.assertTrue(operands.size() == 2);
        Assert.assertTrue(operands.contains("{x}|Queue|{y}|hits"));
        Assert.assertTrue(operands.contains("{x}|Queue|{y}|misses"));
        //Assert.assertTrue(operands.contains("4"));
        String formula = null;
        OperandsHandler operand1 = new OperandsHandler(formula, pathHandler);
        Set<String> nullOperands = operand1.getBaseOperands();
        Assert.assertTrue(nullOperands == null);
    }

    @Test
    public void checkForFirstVariableTest(){
        Set<String> modifiedOperands = Sets.newHashSet();
        modifiedOperands.add("Server1|Queue|{x}|hits");
        String firstVariable = operand.checkForFirstVariable(modifiedOperands);
        Assert.assertTrue(firstVariable.equals("{x}"));
    }

    @Test
    public void getSubstitutedOperandsTest(){
        Set<String> baseOperands = Sets.newHashSet();
        baseOperands.add("{x}|Queue|{y}|hits");
        baseOperands.add("{x}|Queue|{y}|misses");
        Set<String> substitutedOperands = operand.getSubstitutedOperands(baseOperands,"{x}", "Server1");
        Assert.assertTrue(substitutedOperands.contains("Server1|Queue|{y}|hits"));
        Assert.assertTrue(substitutedOperands.contains("Server1|Queue|{y}|misses"));
        Assert.assertFalse(substitutedOperands.contains("{x}|Queue|{y}|hits"));
        Assert.assertFalse(substitutedOperands.contains("{x}|Queue|{y}|misses"));

    }

    @Test
    public void replacePathTest(){
        String path = pathHandler.getSubstitutedPath("{x}|Queue|{y}|hits","{x}", "Server1");
        Assert.assertTrue(path.equals("Server1|Queue|{y}|hits"));
    }

    @Test
    public void getSubstitutedExpressionTest(){
        Set<String> modifiedOperands = Sets.newHashSet();
        modifiedOperands.add("Server1|Queue|Q1|hits");
        modifiedOperands.add("Server1|Queue|Q1|misses");
        //String expression = "{x}|Queue|{y}|hits / ({x}|Queue|{y}|hits + {x}|Queue|{y}|misses)";
        String modifiedExpression = operand.getSubstitutedExpression(modifiedOperands);
        Assert.assertTrue(modifiedExpression.equals("Server1|Queue|Q1|hits / (Server1|Queue|Q1|hits + Server1|Queue|Q1|misses)"));
    }
}
