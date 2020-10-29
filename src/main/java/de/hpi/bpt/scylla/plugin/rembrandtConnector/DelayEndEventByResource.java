package de.hpi.bpt.scylla.plugin.rembrandtConnector;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskBeginEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import desmoj.core.simulator.TimeSpan;
import org.json.*;

import java.util.concurrent.TimeUnit;

public class DelayEndEventByResource extends TaskBeginEventPluggable {

    @Override
    public String getName() {
        return rembrandtConnectorUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(TaskBeginEvent beginEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        int nodeId = beginEvent.getNodeId(); // node (task) of current event
        // get the old value (this will always be the entry 0 in our map, because it's always the next)
        double standardTime = beginEvent.getTimeSpanToNextEventMap().get(0).getTimeAsDouble(TimeUnit.SECONDS);

        // find assigned resource
        String resourceId = findAssignedResourceInstance(beginEvent, processInstance);
        System.out.println("looking for resourceID: " + resourceId);
        //  read/ calculate resource time.
        float resourceBasedTime = getResourceTime(resourceId);
        //check if it is a decimal or flat value
        TimeSpan timeForTaskByResource;
        if (resourceBasedTime == (int)resourceBasedTime){
            timeForTaskByResource = new TimeSpan(resourceBasedTime, TimeUnit.SECONDS);
        }
        else {
            timeForTaskByResource = new TimeSpan(standardTime * resourceBasedTime, TimeUnit.SECONDS);
        }
        // and overwrite the time to the next task in the timeSpanToNextEventMap (=set the calculated time as the new time)

        beginEvent.getTimeSpanToNextEventMap().put(0, timeForTaskByResource);

        return;
    }


    private String findAssignedResourceInstance(TaskBeginEvent beginEvent, ProcessInstance processInstance){
        //look at map from assignment and return the resourceID for current task
        String taskId = Integer.toString(beginEvent.getNodeId());
        String processInstanceId = Integer.toString(beginEvent.getProcessInstance().getId());
        return rembrandtConnectorUtils.resourceTaskMap.get(processInstanceId + "." + taskId);
    }

    private float getResourceTime(String resourceId) {
        String timeAttribute = "";
        float additionalTime = 0;
        JSONObject resource = new JSONObject();
        try {
            resource = new JSONObject(rembrandtConnectorUtils.getResponse(rembrandtConnectorUtils.getBackendUrl() + "/organization/resource-instances/" + resourceId));
        } catch (Exception e) {
            System.out.println("resource " + resourceId + " not found.");
        }
        try {

            JSONArray resourceType = resource.getJSONArray("included");
            timeAttribute = resourceType.getJSONObject(0).getJSONObject("attributes").getString("timeAttribute");
        }
        catch (Exception e) {
            System.out.println("could not read timeAttribute of resourceType.");
        }
        try {
            JSONObject resourceInstance = resource.getJSONObject("data");
            JSONArray attributes  = resourceInstance.getJSONObject("attributes").getJSONArray("attributes");
            additionalTime = Float.parseFloat(attributes.getJSONObject(findIndexOfAttribute(timeAttribute, attributes)).getString("value"));
            System.out.println(timeAttribute + " is " + additionalTime);
        }
        catch (Exception e) {
            System.out.println("error during reading of timeAttribute of resource");
        }
        return additionalTime;
    }

    private Integer findIndexOfAttribute(String attributeName, JSONArray attributesArray)
    {
        Integer i  = 0;
        while (i < attributesArray.length()) {
            if (attributesArray.getJSONObject(i).getString("name").equals(attributeName)){
                return i;
            }
            else {
                i++;
            }
        }
        return null;
    }

}