package com.appdynamics.extensions.util.derived;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class OperandsFetcherTest {
    private OperandsFetcher operandsFetcher = new OperandsFetcher();
    private String nullString;
    private String formula = "hits / (hits + misses) + 4";

    @Test
    public void getOperandsFromFormulaTest(){
        Set<String> operands = operandsFetcher.getOperandsFromFormula(formula);
        Assert.assertTrue(operands.size() == 3);
        Assert.assertTrue(operands.contains("hits"));
        Assert.assertTrue(operands.contains("misses"));
        Assert.assertTrue(operands.contains("4"));
        Set<String> nullOperands = operandsFetcher.getOperandsFromFormula(nullString);
        Assert.assertTrue(nullOperands == null);
    }

}
