package com.appdynamics.extensions.util.derived;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by venkata.konala on 8/28/17.
 */
public class OperandsFetcher {
    private Splitters splitters = new Splitters();

    /* The getBaseMetricsfromFormula(String formula) method takes an expression and
     * returns the set of operands. The operators allowed are +,-,*,/,%,^.
     * To apply precedence, "()" can be used. Spaces can be used in the expression
     * to separate operators and operands. Please note that the operands do not
     * have to be only baseMetrics, they can be numbers also.
     */
    public Set<String> getOperandsFromFormula(String formula){
        Set<String> operands = new HashSet<String>();
        if(formula != null) {
            List<String> baseMetricsList = splitters.getFormulaSplitter().splitToList(formula);
            for (String baseMetric : baseMetricsList) {
                operands.add(baseMetric);
            }
            return operands;
        }
        return null;
    }
}
