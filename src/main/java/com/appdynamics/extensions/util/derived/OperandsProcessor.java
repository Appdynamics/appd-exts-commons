package com.appdynamics.extensions.util.derived;

import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by venkata.konala on 8/28/17.
 */
public class OperandsProcessor {
    private Splitters splitters = new Splitters();
    private Set<String> operands;

    public OperandsProcessor(Set<String> operands){
        this.operands = operands;
    }

    public String checkForFirstVariable(){
        for(String operand : operands){
            if(operand != null) {
                List<String> metricHierarchyList = splitters.getPipeSplitter().splitToList(operand);
                for (String metricHierarchy : metricHierarchyList) {
                    if (metricHierarchy.startsWith("{") && metricHierarchy.endsWith("}")) {
                        return metricHierarchy;
                    }
                }
            }
        }
        return null;
    }

    public Set<String> getModifiedOperands(String variable, String variableValue){
        Set<String> modifiedOperands = Sets.newHashSet();
        for(String operand : operands){
            modifiedOperands.add(getModifiedPath(operand, variable, variableValue));
        }
        return modifiedOperands;
    }

    public String getModifiedPath(String path, String variable, String variableValue){
        if(path.contains(variable)){
            path = path.replace(variable, variableValue);
        }
        return path;
    }

    public String getModifiedExpression(Set<String> modifiedOperands, String formula){
        for(String operand : operands){
            for(String modifiedOperand : modifiedOperands){
                if(match(operand, modifiedOperand)){
                    formula = formula.replace(operand, modifiedOperand);
                    break;
                }
            }
        }
        return formula;
    }

    private boolean match(String operand, String modifiedOperand){
        List<String> operandList = splitters.getPipeSplitter().splitToList(operand);
        List<String> modifiedOperandList = splitters.getPipeSplitter().splitToList(modifiedOperand);
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
