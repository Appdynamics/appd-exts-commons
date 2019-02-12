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

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 8/15/17.
 */
public class ExpressionEvaluatorTest {

    @Test
    public void basicFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1.0 + 1.0) / 2.0");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

    @Test
    public void singleValuesTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("1.0");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

    @Test
    public void noSpaceInFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1+1)/2");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

    @Test
    public void randomSpaceFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1+1 ) / 2");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

    @Test(expected = IllegalExpressionException.class)
    public void incorrectFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("1+1 ) / 2");
        expressionEvaluator.eval();
    }

    @Test(expected = IllegalExpressionException.class)
    public void illegalFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1+1 ) / 0");
        expressionEvaluator.eval() ;
    }

   @Test(expected = IllegalExpressionException.class)
    public void illegalAndIncorrectFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1+1  / 0");
       expressionEvaluator.eval();
    }

    @Test(expected = IllegalExpressionException.class)
    public void wrongNumberFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1m+1  / 0");
        expressionEvaluator.eval();
    }
}
