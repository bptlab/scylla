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
    // hold static map which source+nodeID has which asssignment, to free them in Endevent and to delay tasktime


    //Todo: more than one resource required by scylla? Rembrandt can only handle a single Output atm, start multiple times by resource Type or improve Rembrandt and read the result in a loop.



    @Override
    public String getName() {
        return rembrandtConnectorUtils.PLUGIN_NAME;
    }

    @Override
    public boolean wantsToHandleAssignment(SimulationModel model, ScyllaEvent event) {
        Set<ResourceReference> resourceReferences = event.getSimulationComponents().getSimulationConfiguration().getResourceReferenceSet(event.getNodeId());
        for (ResourceReference ref : resourceReferences) {
            // Todo: change to real resourcename
            if(ref.getResourceId().equals(rembrandtConnectorUtils.getPseudoResourceTypeName())) {
                System.out.println("starting Rembrandt Assignment");
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<ResourceObjectTuple> getResourcesForEvent(SimulationModel model, ScyllaEvent event) {
        rembrandtConnectorUtils.resourceTaskMap.put("1", "5e7b762d7c6d5f10fce4b6a3");
        String recipeId = "";
        // find recipeID based on taskName

        String taskName = event.getDisplayName();
        //Todo: delete this hardcoded name
        taskName = "SMile Tour Planning - Munkres";
        System.out.println("looking for recipe: " + taskName);


        try {
            // TODO: use correct link to backend
            JSONObject recipes = new JSONObject(rembrandtConnectorUtils.getResponse("https://rembrandt.voelker.dev/api/optimization/recipes"));
            recipeId = findRecipeForTask(recipes, taskName);
        } catch (Exception e) {
            //TODO: write taskname and searched recipe into error message
            System.out.println("could not find recipe wit name: " + taskName);
            ResourceObjectTuple emptyTuple = new ResourceObjectTuple();
            return Optional.of(emptyTuple);
        }
        //start recipe by ID Todo: include it
        //JSONObject execution = new JSONObject(rembrandtConnectorUtils.getResponse("https://rembrandt.voelker.dev/api/optimization/recipes/" + recipeId + "/execute"));
        // Todo: wait for result
        // read result from execution

        JSONObject execution = new JSONObject(rembrandtConnectorUtils.getResponse("https://rembrandt.voelker.dev/api/optimization/executions/5df739392b29e200119d8611"));
        String resultTypeId = getResultTypeId(recipeId);
        String resultInstanceId = execution.getJSONObject("data").getJSONObject("attributes").getJSONObject("result").getJSONObject("data").getJSONArray(resultTypeId).getJSONObject(0).getString("_id");
        // write assigned Resource in map

        int taskId = event.getNodeId();
        int processInstanceId = event.getProcessInstance().getId();
        Pair<Integer, Integer> resourceidentifier = new Pair<>(taskId, processInstanceId);

        if (rembrandtConnectorUtils.resourceTaskMap.containsValue(resultInstanceId)) {
            //put in waiting queue
            //Pair < String, String> resourceidentifier = new Pair<> (taskId, processInstanceId);
            rembrandtConnectorUtils.eventsWaitingMap.get(resultInstanceId).add(resourceidentifier);

            return null;
        } else {
            rembrandtConnectorUtils.resourceTaskMap.put(taskId + "." + processInstanceId, resultInstanceId);
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
        JSONObject recipe = new JSONObject(rembrandtConnectorUtils.getResponse("https://rembrandt.voelker.dev/api/optimization/recipes/"+recipeId));
        Integer arrayLength = recipe.getJSONObject("data").getJSONObject("attributes").getJSONArray("ingredients").length();
        return recipe.getJSONObject("data").getJSONObject("attributes").getJSONArray("ingredients").getJSONObject(arrayLength-1).getString("ingredientDefinition");
    }


    public String findRecipeForTask(JSONObject recipes, String taskName) {
        Integer i = 0;
        JSONArray recipearray = recipes.getJSONArray("data");
        while (i < recipearray.length()) {
            if (recipearray.getJSONObject(i).getJSONObject("attributes").getString("name").equals(taskName)){
                return recipearray.getJSONObject(i).getString("id");
            }
            else {
                i++;
            }
        }
        return null;
    }


}

