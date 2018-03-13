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

package com.appdynamics.extensions.metrics.derived;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 8/9/17.
 */
public class  ExpressionEvaluator {
    private String expression;

    public ExpressionEvaluator(String expression){
        this.expression = expression;
    }

    public BigDecimal eval() throws IllegalExpressionException{
        try {
            Expression e = new ExpressionBuilder(expression).build();
            Double result = e.evaluate();
            return new BigDecimal(result);
        }
        catch(Exception exception){
            throw new IllegalExpressionException("The expression " + expression + " is illegal : " + exception);
        }
    }
}

