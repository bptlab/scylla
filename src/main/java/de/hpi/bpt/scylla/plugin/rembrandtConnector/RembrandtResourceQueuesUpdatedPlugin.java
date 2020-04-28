package de.hpi.bpt.scylla.plugin.rembrandtConnector;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.resource.ResourceQueueUpdatedPluggable;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.ScyllaEventQueue;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RembrandtResourceQueuesUpdatedPlugin extends ResourceQueueUpdatedPluggable {

    // this plugin manages waiting events and starts them when a resource is available.
    @Override
    public String getName() {
        return rembrandtConnectorUtils.PLUGIN_NAME;
    }

    @Override
    public ScyllaEvent eventToBeScheduled(SimulationModel model, Set<String> resourceQueuesUpdated){

        System.out.println(resourceQueuesUpdated);
        System.out.println(model.getResourceManager().getResourceTypes());

        // if resource is from Rembrandt
        for (String resourceID : resourceQueuesUpdated ) {
            //if it is not a pre defined resource
            if (!model.getGlobalConfiguration().getResources().containsKey(resourceID) || resourceID.equals("Worker")){

                // return event from queue of pseudoresourcetyp
                ScyllaEventQueue pseudoTypeQueue = model.getEventQueues().get(rembrandtConnectorUtils.getPseudoResourceTypeName());
                for (ResourceObject resource : model.getResourceManager().getResourceObjects().get(rembrandtConnectorUtils.getPseudoResourceTypeName())) {

                    if (rembrandtConnectorUtils.eventsWaitingMap.containsKey(resource.getId())) {
                        // if there are events waiting for this specific resource
                        Pair<Integer, Integer> resourceIdentifier = rembrandtConnectorUtils.eventsWaitingMap.get(resource.getId()).poll();
                        Integer taskId = resourceIdentifier.getValue0();
                        Integer processId = resourceIdentifier.getValue1();

                        // search for correct event in the eventwaiting Queue of the pseudo resource Type
                        ScyllaEventQueue pseudoTypeEventQueue = model.getEventQueues().get(rembrandtConnectorUtils.getPseudoResourceTypeName());
                        List<ScyllaEvent> events = new ArrayList<ScyllaEvent>();
                        for (int i = 0; i < pseudoTypeEventQueue.size(); i++) {
                            if (pseudoTypeEventQueue.peek().getNodeId() == taskId && pseudoTypeEventQueue.peek().getProcessInstance().getId() == processId) {
                                // if the correct event
                                ScyllaEvent eventToBeSheduled = pseudoTypeEventQueue.poll();

                                ResourceObjectTuple assignedResourceTuple = new ResourceObjectTuple();
                                assignedResourceTuple.getResourceObjects().add(resource);
                                model.getResourceManager().assignResourcesToEvent(eventToBeSheduled, assignedResourceTuple);

                                model.removeFromEventQueues(eventToBeSheduled);
                                return eventToBeSheduled;
                            }
                            else {
                                // put head of the queue to the back
                                pseudoTypeEventQueue.add(pseudoTypeQueue.poll());
                            }
                        }
                        // if there is no such event in the eventQueue
                        throw new ScyllaRuntimeException("could not find specified Event in the queue of the pseudo event type!");
                   }
                }

            }
        }
//TODO: what if there is no event in the queue? what should the plugin return? is null ok?
        // if there is no event waiting for this specific resource
        return null;
    }

}
