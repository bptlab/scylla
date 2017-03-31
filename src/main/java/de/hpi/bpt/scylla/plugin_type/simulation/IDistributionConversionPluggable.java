package de.hpi.bpt.scylla.plugin_type.simulation;

import java.util.Map;

import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;

public interface IDistributionConversionPluggable {

    Map<Integer, Object> convertToDesmoJDistributions(ProcessSimulationComponents pSimComponents);

}
