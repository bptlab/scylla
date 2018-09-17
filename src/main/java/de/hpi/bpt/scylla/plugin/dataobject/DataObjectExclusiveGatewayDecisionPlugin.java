package de.hpi.bpt.scylla.plugin.dataobject;

import java.util.Set;
import java.util.function.Predicate;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.plugin.gateway_exclusive.ExclusiveGatewayDecisionPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;

public class DataObjectExclusiveGatewayDecisionPlugin extends ExclusiveGatewayDecisionPluggable{
	
	//TODO find out that information when parsing
	private static enum Operator {
		EQUAL(			i -> i == 0, "==", "="), 
		GREATEROREQUAL(	i -> i >= 0, ">="), 
		LESSOREQUAL(	i -> i <= 0, "<="), 
		NOTEQUAL(		i -> i != 0, "!="), 
		LESS(			i -> i <  0, "<"), 
		GREATER(		i -> i >  0, ">");
		
		private String[] tokens;
		private Predicate<Integer> condition;
		private Operator(Predicate<Integer> c, String... t) {
			condition = c;
			tokens = t;
		}
	}

	@Override
	public String getName() {
		return DataObjectPluginUtils.PLUGIN_NAME;
	}

	@Override
	public Integer decideGateway(GatewayEvent desmojEvent, ProcessInstance processInstance, Integer currentlyChosen) throws ScyllaRuntimeException {
		if(currentlyChosen != null)return null;
		
		try {
			ProcessModel processModel = processInstance.getProcessModel();
			int nodeId = desmojEvent.getNodeId();
			
			Set<Integer> outgoingRefs = processModel.getGraph().getTargetObjects(nodeId);
			for (Integer outgoingFlow : outgoingRefs) { //go through all outgoing references
				String displayName = processModel.getDisplayNames().get(outgoingFlow);
				if(displayName == null || !containsOperator(displayName))continue;
			    String[] conditions = displayName.split("&&");
			    boolean conditionsFulfilled = true;
			    for (String condition : conditions) {
			    	if(!conditionFulfilled(condition, processInstance)) {
			    		conditionsFulfilled = false;
			    		break;
			    	}
			    }
			    if (conditionsFulfilled) {
			    	return outgoingFlow;
			    }
			}
		} catch (NodeNotFoundException | ScyllaValidationException e) {
			e.printStackTrace();
		}

        return null;
	}
	
    private static boolean conditionFulfilled(String condition, ProcessInstance processInstance) throws ScyllaValidationException {
    	
        condition = condition.trim();
        String field = null;
        String value = null;
        Operator comparison = null;
        
    	operatorSearch:for(Operator operator : Operator.values()) {
    		for(String token : operator.tokens) {
    			if(condition.contains(token)) {
    				comparison = operator;
    	            field = condition.split(token)[0];
    	            value = condition.split(token)[1];
    	            break operatorSearch;
    			}
    		}
    	}
        if(comparison == null)throw new ScyllaValidationException("Condition " + condition + " does not have a comparison-operator");

        /*if (condition.contains("==")) {
            field = condition.split("==")[0];
            value = condition.split("==")[1];
            //value = processModel.getDisplayNames().get(or).substring(2, processModel.getDisplayNames().get(or).length());
            comparison = "equal";
        } else if (condition.contains(">=")) {
            field = condition.split(">=")[0];
            value = condition.split(">=")[1];
            comparison = "greaterOrEqual";
        } else if (condition.contains("<=")) {
            field = condition.split("<=")[0];
            value = condition.split("<=")[1];
            comparison = "lessOrEqual";
        } else if (condition.contains("!=")) {
            field = condition.split("!=")[0];
            value = condition.split("!=")[1];
            comparison = "notEqual";
        } else if (condition.contains("=")) {
            field = condition.split("=")[0];
            value = condition.split("=")[1];
            comparison = "equal";
        } else if (condition.contains("<")) {
            field = condition.split("<")[0];
            value = condition.split("<")[1];
            comparison = "less";
        } else if (condition.contains(">")) {
            field = condition.split(">")[0];
            value = condition.split(">")[1];
            comparison = "greater";
        } else {
            throw new ScyllaValidationException("Condition " + condition + " does not have a comparison-operator");
        }*/
        value = value.trim();
        field = field.trim();

        String fieldValue = String.valueOf(DataObjectField.getDataObjectValue(processInstance.getId(), field));

        if (isParsableAsLong(value) && isParsableAsLong(fieldValue)) { //try a long comparison
            Long LongValue = Long.valueOf(value);
            Long dOValue = Long.valueOf(fieldValue);
            Integer comparisonResult = (dOValue.compareTo(LongValue));            
            return comparison.condition.test(comparisonResult);

        } else { //otherwise do a string compare
            Integer comparisonResult = fieldValue.trim().compareTo(String.valueOf(value));
            return 
            	comparison.condition.test(comparisonResult) && (comparison.equals(Operator.EQUAL) || comparison.equals(Operator.NOTEQUAL));
        }
    }
    

    private static boolean isParsableAsLong(final String s) {
        try {
            Long.valueOf(s);
            return true;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
    
    private static boolean containsOperator(String s) {
    	for(Operator operator : Operator.values()) {
    		for(String token : operator.tokens) {
    			if(s.contains(token))return true;
    		}
    	}
    	return false;
    }

}
