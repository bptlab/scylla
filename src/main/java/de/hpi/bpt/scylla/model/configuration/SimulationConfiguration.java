package de.hpi.bpt.scylla.model.configuration;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.SimulationInput;
import de.hpi.bpt.scylla.model.configuration.distribution.TimeDistributionWrapper;
import de.hpi.bpt.scylla.model.process.ProcessModel;

/**
 * Represents all process-specific simulation parameters which are necessary for conducting the simulation.
 * 
 * @author Tsun Yin Wong
 * 
 */
public class SimulationConfiguration extends SimulationInput {

    private ProcessModel processModel;
    private Integer numberOfProcessInstances;
    private ZonedDateTime startDateTime;
    private ZonedDateTime endDateTime;
    private Long randomSeed;

    private Map<Integer, TimeDistributionWrapper> arrivalRates;
    private Map<Integer, TimeDistributionWrapper> durations;
    private Map<Integer, TimeDistributionWrapper> setUpDurations;

    private Map<Integer, Set<ResourceReference>> resourceReferences;
    private Map<Integer, SimulationConfiguration> configurationsOfSubProcesses;

    /**
     * Constructor.
     * 
     * @param id
     *            identifier of simulation configuration
     * @param processModel
     *            process model which is referenced by the simulation configuration
     * @param numberOfProcessInstances
     *            number of process instances to be simulated
     * @param startDateTime
     *            start date time of simulation
     * @param endDateTime
     *            end date time of simulation
     * @param randomSeed
     *            random seed of simulation (overrides random seed of global configuration)
     * @param arrivalRates
     *            arrival rates of new process instances
     * @param durations
     *            task durations
     * @param setUpDurations
     *            task set up durations
     * @param resourceReferences
     *            definition of resources involved in a task
     * @param configurationsOfSubProcesses
     *            simulation configurations of sub processes of {@link #processModel}
     */
    public SimulationConfiguration(String id, ProcessModel processModel, Integer numberOfProcessInstances,
            ZonedDateTime startDateTime, ZonedDateTime endDateTime, Long randomSeed,
            Map<Integer, TimeDistributionWrapper> arrivalRates, Map<Integer, TimeDistributionWrapper> durations, Map<Integer,
            TimeDistributionWrapper> setUpDurations,
            Map<Integer, Set<ResourceReference>> resourceReferences,
            Map<Integer, SimulationConfiguration> configurationsOfSubProcesses) {
        super(id);
        this.processModel = processModel;
        this.numberOfProcessInstances = numberOfProcessInstances;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.randomSeed = randomSeed;
        this.arrivalRates = arrivalRates;
        this.durations = durations;
        this.setUpDurations = setUpDurations;
        this.resourceReferences = resourceReferences;
        this.configurationsOfSubProcesses = configurationsOfSubProcesses;
    }

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public int getNumberOfProcessInstances() {
        return numberOfProcessInstances;
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public ZonedDateTime getEndDateTime() {
        return endDateTime;
    }

    public Long getRandomSeed() {
        return randomSeed;
    }

    public Map<Integer, TimeDistributionWrapper> getArrivalRates() {
        return arrivalRates;
    }

    public Map<Integer, TimeDistributionWrapper> getDurations() {
        return durations;
    }

    public Map<Integer, Set<ResourceReference>> getResourceReferences() {
        return resourceReferences;
    }

    public Set<ResourceReference> getResourceReferenceSet(Integer nodeId) {
        Set<ResourceReference> resourceRefSet = resourceReferences.get(nodeId);
        if (resourceRefSet == null) {
            resourceRefSet = new HashSet<ResourceReference>();
        }
        return resourceRefSet;
    }

    public Map<Integer, TimeDistributionWrapper> getSetUpDurations() {
        return setUpDurations;
    }

    public Map<Integer, SimulationConfiguration> getConfigurationsOfSubProcesses() {
        return configurationsOfSubProcesses;
    }

	public SimulationConfiguration mergeWith(SimulationConfiguration other) throws ScyllaValidationException{
		if(processModel != other.processModel)throw new ScyllaValidationException("Trying to merge simulationconfigurations of different process models");
		
		return new SimulationConfiguration(
				id, 
				processModel, 
				merge(this.numberOfProcessInstances, other.numberOfProcessInstances), 
				merge(this.startDateTime, other.startDateTime), 
				merge(this.endDateTime, other.endDateTime), 
				merge(this.randomSeed, other.randomSeed), 
				merge(this.arrivalRates, other.arrivalRates), 
				merge(this.durations, other.durations), 
				merge(this.setUpDurations, other.setUpDurations), 
				merge(this.resourceReferences, other.resourceReferences), 
				merge(this.configurationsOfSubProcesses, other.configurationsOfSubProcesses));
	}
	
	private static <T> T merge(T a, T b) throws ScyllaValidationException {
		if(a == b || b.equals(null))return a;
		if(a.equals(null)) return b;
		throw new ScyllaValidationException("Conflicting attributes while merging simulationconfigurations");
	}
	
	private static<X,Y> Map<X,Y> merge(Map<X,Y> a, Map<X,Y> b) throws ScyllaValidationException{
		if(a == null)return b;
		if(b == null)return a;
		Stream<Entry<X,Y>> combined = Stream.concat(a.entrySet().stream(), b.entrySet().stream());
		try{
			return (Map<X, Y>) combined.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}catch(IllegalStateException e) {
			throw new ScyllaValidationException("Conflicting keys while merging simulationconfigurations", e);
		}
	}

}
