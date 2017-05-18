package de.hpi.bpt.scylla.plugin.gateway_exclusive;

import java.util.HashMap;
import java.util.Map;

import de.hpi.bpt.scylla.model.configuration.BranchingBehavior;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.DistributionConversionPluggable;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import desmoj.core.dist.DiscreteDistEmpirical;

public class ExclusiveGatewayDistributionConversionPlugin extends DistributionConversionPluggable {

    @Override
    public String getName() {
        return ExclusiveGatewayPluginUtils.PLUGIN_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Integer, Object> convertToDesmoJDistributions(ProcessSimulationComponents pSimComponents) {

        Map<Integer, Object> branchingDistributionsExclusive = new HashMap<Integer, Object>();

        SimulationConfiguration simulationConfiguration = pSimComponents.getSimulationConfiguration();
        Map<Integer, BranchingBehavior> branchingBehaviors = (Map<Integer, BranchingBehavior>) simulationConfiguration
                .getExtensionValue(getName(), "branchingBehaviors");
        Long randomSeed = simulationConfiguration.getRandomSeed();
        ProcessModel processModel = pSimComponents.getProcessModel();
        SimulationModel model = pSimComponents.getModel();
        boolean showInReport = model.reportIsOn();
        boolean showInTrace = model.traceIsOn();

        for (Integer nodeId : branchingBehaviors.keySet()) {
            BranchingBehavior branchingBehavior = branchingBehaviors.get(nodeId);
            Map<Integer, Double> branchingProbabilities = branchingBehavior.getBranchingProbabilities();
            String name = processModel.getModelScopeId() + "_" + nodeId.toString();
            DiscreteDistEmpirical<Integer> desmojDist = new DiscreteDistEmpirical<Integer>(model, name, showInReport,
                    showInTrace);
            for (Integer nextNodeId : branchingProbabilities.keySet()) {
                Double probability = branchingProbabilities.get(nextNodeId);
                desmojDist.addEntry(nextNodeId, probability);
            }
            desmojDist.setSeed(randomSeed);
            branchingDistributionsExclusive.put(nodeId, desmojDist);
        }

        return branchingDistributionsExclusive;
    }

}
