package com.appdynamics.extensions.metrics.derived;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.appdynamics.extensions.util.MetricPathUtils.PIPE_SPLITTER;

/**
 * Created by venkata.konala on 8/28/17.
 */
public class OperandsHandler {

    private String formula;
    private Set<String> baseOperands;
    private DerivedMetricsPathHandler pathHandler;
    static final Splitter FORMULA_SPLITTER = Splitter.on(CharMatcher.anyOf("(+-*/%^)"))
            .trimResults()
            .omitEmptyStrings();

    OperandsHandler(String formula, DerivedMetricsPathHandler pathHandler){
        this.formula = formula;
        baseOperands = getOperandsFromFormula();
        this.pathHandler = pathHandler;
    }

    /* This method takes an expression and
    * returns the set of baseOperands. The operators allowed are +,-,*,/,%,^.
    * To apply precedence, "()" can be used. Spaces can be used in the expression
    * to separate operators and baseOperands. Please note that the baseOperands do not
    * have to be only baseMetrics, they can be numbers also.
    */
    private Set<String> getOperandsFromFormula(){
        Set<String> operands = new HashSet<String>();
        if(formula != null){
            List<String> baseMetricsList = FORMULA_SPLITTER.splitToList(formula);
            for (String baseMetric : baseMetricsList) {
                operands.add(baseMetric);
            }
            return operands;
        }
        return null;
    }

    Set<String> getBaseOperands(){
        return baseOperands;
    }

     String checkForFirstVariable(Set<String> substitutedOperands){
        for(String operand : substitutedOperands){
            if(operand != null) {
                List<String> metricHierarchyList = PIPE_SPLITTER.splitToList(operand);
                for (String metricHierarchy : metricHierarchyList) {
                    if (metricHierarchy.startsWith("{") && metricHierarchy.endsWith("}")) {
                        return metricHierarchy;
                    }
                }
            }
        }
        return null;
     }

     Set<String> getSubstitutedOperands(Set<String> baseOperands, String variable, String variableValue){
        Set<String> modifiedOperands = Sets.newHashSet();
        for(String operand : baseOperands){
            modifiedOperands.add(pathHandler.getSubstitutedPath(operand, variable, variableValue));
        }
        return modifiedOperands;
     }



     String getSubstitutedExpression(Set<String> substitutedOperands){
         String modifiedExpression = formula;
         for(String operand : baseOperands){
            for(String modifiedOperand : substitutedOperands){
                if(match(operand, modifiedOperand)){
                    modifiedExpression = modifiedExpression.replace(operand, modifiedOperand);
                    break;
                }
            }
         }
         return modifiedExpression;
     }

    public static boolean match(String operand, String modifiedOperand){
        List<String> operandList = PIPE_SPLITTER.splitToList(operand);
        List<String> modifiedOperandList = PIPE_SPLITTER.splitToList(modifiedOperand);
        if(operandList.size() == modifiedOperandList.size()){
            Iterator operandListIterator = operandList.iterator();
            Iterator modifiedOperandListIterator = modifiedOperandList.iterator();
            while(operandListIterator.hasNext() && modifiedOperandListIterator.hasNext()){
                String operandSplit = operandListIterator.next().toString();
                String modifiedOperandSplit = modifiedOperandListIterator.next().toString();
                if(operandSplit.startsWith("{") && operandSplit.endsWith("}")){
                    continue;
                }
                else if(!operandSplit.equals(modifiedOperandSplit)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
