package de.hpi.bpt.scylla.plugin.rembrandtConnector;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.hpi.bpt.scylla.plugin_type.simulation.resource.ResourceAssignmentPluggable;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import org.json.JSONArray;
import org.json.JSONObject;

public class resourceAssignmentPlugin extends ResourceAssignmentPluggable {
    // Ask rembrandt to assign a ResourceInstance and convert it to scylla readable assignment --> see function in QueueManager
    // hold static map which source+nodeID has which asssignment, to free them in Endevent and to delay tasktime

    //Todo: more than one resource assigned? (read result in loop + write all into map + free all later)

    public static Map<String, String> resourceTaskMap = new HashMap<>();

    @Override
    public String getName() {
        return rembrandtConnectorUtils.PLUGIN_NAME;
    }

    @Override
    public Optional<ResourceObjectTuple> getResourcesForEvent(SimulationModel model, ScyllaEvent event)

    {
        resourceTaskMap.put("1", "5e7b762d7c6d5f10fce4b6a3");
        String recipeId = "";
        // find recipeID based on taskName

        String taskName = event.getDisplayName();
        //Todo: delete this hardcoded name
        taskName = "SMile Tour Planning - Munkres";



        try {
            // TODO: use correct link to backend
            JSONObject recipes = new JSONObject(rembrandtConnectorUtils.getResponse("https://rembrandt.voelker.dev/api/optimization/recipes"));
            recipeId = findRecipeForTask(recipes, taskName);
        } catch (Exception e)
        {
            //TODO: write taskname and searched recipe into error message
           System.out.println("could not find recipe for this task");
        }
        //start recipe by ID Todo: include it
        //JSONObject execution = new JSONObject(rembrandtConnectorUtils.getResponse("https://rembrandt.voelker.dev/api/optimization/recipes/" + recipeId + "/execute"));
        // Todo: wait for result
        // read result from execution
        JSONObject execution = new JSONObject(rembrandtConnectorUtils.getResponse("https://rembrandt.voelker.dev/api/optimization/executions/5df739392b29e200119d8611"));
        String resultTypeId = getResultTypeId(recipeId);
        String resultInstanceId = execution.getJSONObject("data").getJSONObject("attributes").getJSONObject("result").getJSONObject("data").getJSONArray(resultTypeId).getJSONObject(0).getString("_id");
        // write assigned Resource in map

        String taskId = Integer.toString(event.getNodeId());
        String processInstanceId = Integer.toString(event.getProcessInstance().getId());

        resourceTaskMap.put(taskId + "." + processInstanceId, resultInstanceId);

        //Todo: return resource Object Tuple
        return Optional.empty();
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

