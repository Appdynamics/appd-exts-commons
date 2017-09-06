package com.appdynamics.extensions.util.derived;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import static com.appdynamics.extensions.util.derived.Constants.formulaSplitter;
import static com.appdynamics.extensions.util.derived.Constants.pipeSplitter;

/**
 * Created by venkata.konala on 8/28/17.
 */
class Operand {

    private String formula;
    private Set<String> baseOperands;

    public Operand(String formula){
        this.formula = formula;
        baseOperands = getOperandsFromFormula();
    }

    /* The getBaseMetricsfromFormula(String formula) method takes an expression and
    * returns the set of baseOperands. The operators allowed are +,-,*,/,%,^.
    * To apply precedence, "()" can be used. Spaces can be used in the expression
    * to separate operators and baseOperands. Please note that the baseOperands do not
    * have to be only baseMetrics, they can be numbers also.
    */
    private Set<String> getOperandsFromFormula(){
        Set<String> operands = new HashSet<String>();
        if(formula != null){
            List<String> baseMetricsList = formulaSplitter.splitToList(formula);
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
                List<String> metricHierarchyList = pipeSplitter.splitToList(operand);
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
            modifiedOperands.add(getSubstitutedPath(operand, variable, variableValue));
        }
        return modifiedOperands;
    }

     String getSubstitutedPath(String path, String variable, String variableValue){
        if(path.contains(variable)){
            path = path.replace(variable, variableValue);
        }
        return path;
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

    private boolean match(String operand, String modifiedOperand){
        List<String> operandList = pipeSplitter.splitToList(operand);
        List<String> modifiedOperandList = pipeSplitter.splitToList(modifiedOperand);
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
