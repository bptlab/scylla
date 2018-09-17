package de.hpi.bpt.scylla.plugin.dataobject;

import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.plugin.gateway_exclusive.ExclusiveGatewayDecisionPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;

public class DataObjectExclusiveGatewayDecisionPlugin extends ExclusiveGatewayDecisionPluggable{

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
			    String[] conditions = processModel.getDisplayNames().get(outgoingFlow).split("&&");
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
        String comparison = null;

        if (condition.contains("==")) {
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
        }
        value = value.trim();
        field = field.trim();

        String fieldValue = String.valueOf(DataObjectField.getDataObjectValue(processInstance.getId(), field));

        if (isParsableAsLong(value) && isParsableAsLong(fieldValue)) { //try a long comparison
            Long LongValue = Long.valueOf(value);
            Long dOValue = Long.valueOf(fieldValue);
            Integer comparisonResult = (dOValue.compareTo(LongValue));

            return 
            	(comparison.equals("equal") && comparisonResult == 0)
            	|| (comparison.equals("less") && comparisonResult < 0)
            	|| (comparison.equals("greater") && comparisonResult > 0)
            	|| (comparison.equals("greaterOrEqual") && comparisonResult >= 0)
            	|| (comparison.equals("lessOrEqual") && comparisonResult <= 0);

        } else { //otherwise do a string compare
            Integer comparisonResult = fieldValue.trim().compareTo(String.valueOf(value));
            return 
            	(comparison.equals("equal") && comparisonResult == 0)
                || (comparison.equals("notEqual") && comparisonResult != 0);
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

}
