package de.hpi.bpt.scylla.plugin.rembrandtConnector;
import java.util.*;

import de.hpi.bpt.scylla.model.configuration.ResourceReference;
import de.hpi.bpt.scylla.plugin_type.simulation.resource.ResourceAssignmentPluggable;
import de.hpi.bpt.scylla.simulation.*;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONObject;


public class resourceAssignmentPlugin extends ResourceAssignmentPluggable {
    // Ask rembrandt to assign a ResourceInstance and convert it to scylla readable assignment --> see function in QueueManager
    // hold static map which source+nodeID has which assignment, to free them in end-event and to delay tasktime


    //Todo: more than one resource required by scylla? Rembrandt can only handle a single Output atm, start multiple times by resource Type or improve Rembrandt and read the result in a loop.



    @Override
    public String getName() {
        return rembrandtConnectorUtils.PLUGIN_NAME;
    }

    @Override
    public boolean wantsToHandleAssignment(SimulationModel model, ScyllaEvent event) {
        Set<ResourceReference> resourceReferences = event.getSimulationComponents().getSimulationConfiguration().getResourceReferenceSet(event.getNodeId());
        for (ResourceReference ref : resourceReferences) {
            if(ref.getResourceId().equals(rembrandtConnectorUtils.getPseudoResourceTypeName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<ResourceObjectTuple> getResourcesForEvent(SimulationModel model, ScyllaEvent event) {

        String recipeId = "";
        // if activity is already scheduled and waiting, do not schedule it again
        int taskId = event.getNodeId();
        int processInstanceId = event.getProcessInstance().getId();
        Pair<Integer, Integer> taskidentifier = new Pair<>(taskId, processInstanceId);

        for ( Map.Entry<String, Queue<Pair<Integer, Integer>>> resourceId : rembrandtConnectorUtils.eventsWaitingMap.entrySet()){
            Queue<Pair<Integer, Integer>> taskQueue = resourceId.getValue();
            for (Pair<Integer, Integer> task : taskQueue){
                if (task.equals(taskidentifier)){
                    System.out.println("already sheduled");
                    return Optional.empty();
                }
            }
        }


        // find recipeID based on taskName
        System.out.println("starting Rembrandt Assignment");
        String taskName = event.getDisplayName();
        //Todo: delete this hardcoded name
        taskName = "SMile Tour Planning - Rule";
        System.out.println("looking for recipe: " + taskName);


        try {
            JSONObject recipes = new JSONObject(rembrandtConnectorUtils.getResponse(rembrandtConnectorUtils.getBackendUrl()+"/optimization/recipes"));
            recipeId = findRecipeForTask(recipes, taskName);
        } catch (Exception e) {
            System.out.println("Error: could not find recipe wit name: " + taskName);
            ResourceObjectTuple emptyTuple = new ResourceObjectTuple();
            return Optional.of(emptyTuple);
        }
        //start recipe by ID
        JSONObject startExecution = new JSONObject(rembrandtConnectorUtils.getResponse(rembrandtConnectorUtils.getBackendUrl()+ "/optimization/recipes/" + recipeId + "/execute"));
        System.out.println(startExecution.getJSONObject("data").getString("id"));
        String executionId = startExecution.getJSONObject("data").getString("id");

        // wait for result
        boolean finished = false;
        JSONObject execution = new JSONObject(rembrandtConnectorUtils.getResponse(rembrandtConnectorUtils.getBackendUrl()+ "/optimization/executions/" +executionId));
        while (!finished){
            try {
                execution = new JSONObject(rembrandtConnectorUtils.getResponse(rembrandtConnectorUtils.getBackendUrl()+ "/optimization/executions/" +executionId));
                System.out.println("Optimisation finished at: " + execution.getJSONObject("data").getJSONObject("attributes").getString("finishedAt"));
                finished = true;
            } catch (Exception e) {
                System.out.println("waiting for Rembrandt optimisation");
            }

        }

        // read result from execution
        String resultTypeId = getResultTypeId(recipeId);
        String resultInstanceId = execution.getJSONObject("data").getJSONObject("attributes").getJSONObject("result").getJSONObject("data").getJSONArray(resultTypeId).getJSONObject(0).getString("_id");


        // check if resource is free and write it in corresponding map

        if (rembrandtConnectorUtils.resourceTaskMap.containsValue(resultInstanceId)) {
            //put in waiting queue
            if (rembrandtConnectorUtils.eventsWaitingMap.containsKey(resultInstanceId)){
                rembrandtConnectorUtils.eventsWaitingMap.get(resultInstanceId).add(taskidentifier);
            }
            else {
                Queue<Pair<Integer, Integer>> waitingEventsQueue = new PriorityQueue<>();
                waitingEventsQueue.add(taskidentifier);
                rembrandtConnectorUtils.eventsWaitingMap.put(resultInstanceId, waitingEventsQueue);
            }


            return Optional.empty();
        } else {
            rembrandtConnectorUtils.resourceTaskMap.put(processInstanceId + "." + taskId, resultInstanceId);
        }
        //build resourceObject
        int size = model.getResourceManager().getResourceObjects().get(rembrandtConnectorUtils.getPseudoResourceTypeName()).size();
        boolean isPresent = false;
        ResourceObject assignedResource = new ResourceObject(rembrandtConnectorUtils.getPseudoResourceTypeName(), resultInstanceId);
        for (int i = 0; i < size; i++) {
            if (model.getResourceManager().getResourceObjects().get(rembrandtConnectorUtils.getPseudoResourceTypeName()).peek().getId() == resultInstanceId ) {
                assignedResource = model.getResourceManager().getResourceObjects().get(rembrandtConnectorUtils.getPseudoResourceTypeName()).poll();
                break;
            }
            else{
                model.getResourceManager().getResourceObjects().get(rembrandtConnectorUtils.getPseudoResourceTypeName()).add(model.getResourceManager().getResourceObjects().get(rembrandtConnectorUtils.getPseudoResourceTypeName()).poll());
            }

        }

        // does not have to be added to the queue, because it is assigned immediately (thus would be removed from the queue again)
        //build resourceObjectTuple
        ResourceObjectTuple assignedResourceTuple = new ResourceObjectTuple();
        assignedResourceTuple.getResourceObjects().add(assignedResource);
        Optional<ResourceObjectTuple> assignedTuple = Optional.ofNullable(assignedResourceTuple);
        return assignedTuple;
    }

    public String getResultTypeId(String recipeId) {
        // return the resourceType of the recipe result
        JSONObject recipe = new JSONObject(rembrandtConnectorUtils.getResponse(rembrandtConnectorUtils.getBackendUrl()+"/optimization/recipes/"+recipeId));
        Integer arrayLength = recipe.getJSONObject("data").getJSONObject("attributes").getJSONArray("ingredients").length();
        return recipe.getJSONObject("data").getJSONObject("attributes").getJSONArray("ingredients").getJSONObject(arrayLength-1).getString("ingredientDefinition");
    }


    public String findRecipeForTask(JSONObject recipes, String taskName) {
        // selects the correct recipeID based on the task name
        Integer i = 0;
        JSONArray recipeArray = recipes.getJSONArray("data");
        while (i < recipeArray.length()) {
            if (recipeArray.getJSONObject(i).getJSONObject("attributes").getString("name").equals(taskName)){
                return recipeArray.getJSONObject(i).getString("id");
            }
            else {
                i++;
            }
        }
        return null;
    }


}

