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
        String resourceId = findAssignedResourceInstance();
        //  read/ calculate resource time.
        int resourceBasedTime = getResourceTime(resourceAssignmentPlugin.resourceTaskMap.get("1"));
        // and overwrite the time to the next task in the timeSpanToNextEventMap (=set the calculated time as the new time)
        TimeSpan timeForTaskByResource = new TimeSpan(standardTime + resourceBasedTime, TimeUnit.SECONDS);
        beginEvent.getTimeSpanToNextEventMap().put(0, timeForTaskByResource);

    }


    private String findAssignedResourceInstance(){
        //look at map from assignment
        //
        return "ID";
    }

    private Integer getResourceTime(String resourceId) {
        String timeAttribute = "";
        Integer additionalTime = 0;
        JSONObject resource = new JSONObject(rembrandtConnectorUtils.getResponse("http://localhost:3000/api/organization/resource-instances/" + resourceId));
        try {
            JSONArray resourceType = resource.getJSONArray("included");
            timeAttribute = resourceType.getJSONObject(0).getJSONObject("attributes").getString("timeAttribute");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            JSONObject resourceInstance = resource.getJSONObject("data");
            JSONArray attributes  = resourceInstance.getJSONObject("attributes").getJSONArray("attributes");
            additionalTime = Integer.parseInt(attributes.getJSONObject(findIndexOfAttribute(timeAttribute, attributes)).getString("value"));
            System.out.println(timeAttribute + " is " + additionalTime);
        }
        catch (Exception e) {
            System.out.println("error during reading of timeAttribute of resource");
            e.printStackTrace();
        }
        return additionalTime;
    }

    private Integer findIndexOfAttribute(String attributeName, JSONArray attributesArray)
    {
        Integer i  = 0;
        while (i < attributesArray.length()) {
            if (attributesArray.getJSONObject(i).getString("name").equals(attributeName)){
                System.out.println("found it!");
                return i;
            }
            else {
                i++;
            }
        }
        return null;
    }

}