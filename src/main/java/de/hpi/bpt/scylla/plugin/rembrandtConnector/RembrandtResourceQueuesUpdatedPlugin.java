package de.hpi.bpt.scylla.plugin.rembrandtConnector;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.resource.ResourceQueueUpdatedPluggable;
import de.hpi.bpt.scylla.simulation.*;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import org.javatuples.Pair;

import java.util.Set;

public class RembrandtResourceQueuesUpdatedPlugin extends ResourceQueueUpdatedPluggable {

    // this plugin manages waiting events and starts them when a resource is available.
    @Override
    public String getName() {
        return rembrandtConnectorUtils.PLUGIN_NAME;
    }

    @Override
    public ScyllaEvent eventToBeScheduled(SimulationModel model, Set<String> resourceQueuesUpdated){

System.out.println("now looking for waiting event");
        System.out.println("of Type: " + resourceQueuesUpdated);
        // if resource is from Rembrandt
        for (String resourceID : resourceQueuesUpdated ) {
            //if it is not a pre defined resource
            if (!model.getGlobalConfiguration().getResources().containsKey(resourceID) || resourceID.equals(rembrandtConnectorUtils.getPseudoResourceTypeName())){
                System.out.println("it was a Rembrandt resource!");
                // return event from queue of pseudoresourcetyp
                ScyllaEventQueue pseudoTypeQueue = model.getEventQueues().get(rembrandtConnectorUtils.getPseudoResourceTypeName());
                System.out.println("in Eventqueue waiting: ");
                System.out.println(pseudoTypeQueue);
                for (ResourceObject resource : model.getResourceManager().getResourceObjects().get(rembrandtConnectorUtils.getPseudoResourceTypeName())) {
                    System.out.println("checking for resource: " + resource.getId());
                    if (rembrandtConnectorUtils.eventsWaitingMap.containsKey(resource.getId())) {
                        // if there are events waiting for this specific resource
                        System.out.println("there are events waiting for this specific resource!");
                        Pair<Integer, Integer> taskIdentifier = rembrandtConnectorUtils.eventsWaitingMap.get(resource.getId()).poll();
                        //if it was the last waiting event, delete the entry
                        if (rembrandtConnectorUtils.eventsWaitingMap.get(resource.getId()).isEmpty()) {
                            rembrandtConnectorUtils.eventsWaitingMap.remove(resource.getId());
                        }
                        System.out.println("taskidentifier: " + taskIdentifier);
                        Integer taskId = taskIdentifier.getValue0();
                        Integer processId = taskIdentifier.getValue1();
                        System.out.println("all waiting events: " + rembrandtConnectorUtils.eventsWaitingMap.get(resource.getId()));
                        System.out.println("longest waiting Event: processId: " + processId + " and taskId: "+ taskId);

                        // search for correct event in the eventwaiting Queue of the pseudo resource Type
                        ScyllaEventQueue pseudoTypeEventQueue = model.getEventQueues().get(rembrandtConnectorUtils.getPseudoResourceTypeName());

                        for (int i = 0; i < pseudoTypeEventQueue.size(); i++) {
                            if (pseudoTypeEventQueue.peek().getNodeId() == taskId && pseudoTypeEventQueue.peek().getProcessInstance().getId() == processId) {
                                // if the correct event
                                System.out.println("PseudotypeeventQueue: " + pseudoTypeEventQueue);
                                for (ScyllaEvent ev : pseudoTypeEventQueue){
                                    System.out.println(ev.getNodeId() + " " + ev.getProcessInstance().getId());
                                }
                                ScyllaEvent eventToBeSheduled = pseudoTypeEventQueue.poll();
                                System.out.println("PseudotypeeventQueue after poll: " + pseudoTypeEventQueue);
                                ResourceObjectTuple assignedResourceTuple = new ResourceObjectTuple();
                                assignedResourceTuple.getResourceObjects().add(resource);
                                // assign resource to event
                                model.getResourceManager().assignResourcesToEvent(eventToBeSheduled, assignedResourceTuple);
                                //iterate over all resources to remove the assigned resource from available resources

                                model.getResourceManager().getResourceObjects().get(rembrandtConnectorUtils.getPseudoResourceTypeName()).remove(resource);
                                model.removeFromEventQueues(eventToBeSheduled);
                                System.out.println("returning event: " + eventToBeSheduled);
                                return eventToBeSheduled;
                            }
                            else {
                                // put head of the queue to the back
                                pseudoTypeEventQueue.add(pseudoTypeQueue.poll());
                            }
                        }
                        // if there is no such event in the eventQueue
                        //throw new ScyllaRuntimeException("could not find specified Event in the queue of the pseudo event type!");
                   }
                }

            }
        }
        // if there is no event waiting for this specific resource
        return null;
    }

}
