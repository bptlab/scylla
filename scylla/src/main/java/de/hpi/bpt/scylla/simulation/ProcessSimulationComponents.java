package de.hpi.bpt.scylla.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.configuration.distribution.BinomialDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.ConstantDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.Distribution;
import de.hpi.bpt.scylla.model.configuration.distribution.EmpiricalDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.ErlangDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.ExponentialDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.NormalDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.PoissonDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.TriangularDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.UniformDistribution;
import de.hpi.bpt.scylla.model.process.CommonProcessElements;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.DistributionConversionPluggable;
import desmoj.core.dist.ContDistErlang;
import desmoj.core.dist.ContDistExponential;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistTriangular;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.dist.DiscreteDistBinomial;
import desmoj.core.dist.DiscreteDistConstant;
import desmoj.core.dist.DiscreteDistEmpirical;
import desmoj.core.dist.DiscreteDistPoisson;
import desmoj.core.dist.NumericalDist;

/**
 * Container class for process model and all configurations related to this process model, and also for ready-to-use
 * DesmoJ distributions.
 * 
 * @author Tsun
 *
 */
public class ProcessSimulationComponents {

    private SimulationModel model;
    private ProcessSimulationComponents parent;
    private CommonProcessElements commonProcessElements;
    private ProcessModel processModel;
    private SimulationConfiguration simulationConfiguration;

    private Map<Integer, ProcessSimulationComponents> children = new HashMap<Integer, ProcessSimulationComponents>();

    private Map<Integer, NumericalDist<?>> distributions = new HashMap<Integer, NumericalDist<?>>();
    private Map<Integer, TimeUnit> distributionTimeUnits = new HashMap<Integer, TimeUnit>();

    private boolean showInReport;
    private boolean showInTrace;

    private int processInstancesStarted = 0;
    private Map<String, Map<Integer, Object>> extensionDistributions;

    /**
     * Constructor.
     * 
     * @param model
     *            simulation model
     * @param parent
     *            container class of parent process model
     * @param processModel
     *            the process model
     * @param simulationConfiguration
     *            the simulation configuration of the process model
     */
    public ProcessSimulationComponents(SimulationModel model, ProcessSimulationComponents parent,
            ProcessModel processModel, SimulationConfiguration simulationConfiguration) {
        this.model = model;
        this.parent = parent;
        this.processModel = processModel;
        this.simulationConfiguration = simulationConfiguration;

        this.showInReport = model.traceIsOn();
        this.showInTrace = model.reportIsOn();
    }

    public void init() {
        try {
            Map<Integer, Distribution> arrivalRates = simulationConfiguration.getArrivalRates();
            Map<Integer, Distribution> durations = simulationConfiguration.getDurations();

            Map<Integer, Distribution> arrivalRatesAndDurations = new HashMap<Integer, Distribution>();
            arrivalRatesAndDurations.putAll(arrivalRates);
            arrivalRatesAndDurations.putAll(durations);

            for (Integer nodeId : arrivalRatesAndDurations.keySet()) {
                Distribution dist = arrivalRatesAndDurations.get(nodeId);
                TimeUnit distTimeUnit = dist.getTimeUnit();
                if (distTimeUnit.ordinal() < model.getSmallestTimeUnit().ordinal()) {
                    model.setSmallestTimeUnit(distTimeUnit);
                }
            }
            convertToDesmojDistributions(arrivalRatesAndDurations);
            extensionDistributions = DistributionConversionPluggable.runPlugins(this);

            Map<Integer, SimulationConfiguration> configurationsOfSubProcesses = simulationConfiguration
                    .getConfigurationsOfSubProcesses();
            for (Integer nodeId : configurationsOfSubProcesses.keySet()) {
                SimulationConfiguration scOfSubProcess = configurationsOfSubProcesses.get(nodeId);
                ProcessModel pmOfSubProcess = processModel.getSubProcesses().get(nodeId);
                ProcessSimulationComponents desmojObjects = new ProcessSimulationComponents(model, this, pmOfSubProcess,
                        scOfSubProcess);
                desmojObjects.init();
                children.put(nodeId, desmojObjects);
            }
        }
        catch (InstantiationException e) {
            DebugLogger.error(e.getMessage());
            DebugLogger.error("Instantiation of simulation model failed.");
        }
    }

    private void convertToDesmojDistributions(Map<Integer, Distribution> arrivalRatesAndDurations)
            throws InstantiationException {
        distributions = new HashMap<Integer, NumericalDist<?>>();
        Long randomSeed = simulationConfiguration.getRandomSeed();
        for (Integer nodeId : arrivalRatesAndDurations.keySet()) {

            Distribution dist = arrivalRatesAndDurations.get(nodeId);
            TimeUnit distTimeUnit = dist.getTimeUnit();
            distributionTimeUnits.put(nodeId, distTimeUnit);
            String name = processModel.getModelScopeId() + "_" + nodeId.toString();
            NumericalDist<?> desmojDist;
            if (dist instanceof BinomialDistribution) {
                BinomialDistribution binDist = (BinomialDistribution) dist;
                double probability = binDist.getProbability();
                int amount = binDist.getAmount();
                desmojDist = new DiscreteDistBinomial(model, name, probability, amount, showInReport, showInTrace);
            }
            else if (dist instanceof ConstantDistribution) {
                ConstantDistribution conDist = (ConstantDistribution) dist;
                double constantValue = conDist.getConstantValue();
                desmojDist = new DiscreteDistConstant<Number>(model, name, constantValue, showInReport, showInTrace);
            }
            else if (dist instanceof EmpiricalDistribution) {
                EmpiricalDistribution empDist = (EmpiricalDistribution) dist;
                Map<Double, Double> entries = empDist.getEntries();
                DiscreteDistEmpirical<Double> cde = new DiscreteDistEmpirical<Double>(model, name, showInReport,
                        showInTrace);
                for (Double value : entries.keySet()) {
                    Double frequency = entries.get(value);
                    cde.addEntry(value, frequency);
                }
                desmojDist = cde;
            }
            else if (dist instanceof ErlangDistribution) {
                ErlangDistribution erlDist = (ErlangDistribution) dist;
                double mean = erlDist.getMean();
                long order = erlDist.getOrder();
                desmojDist = new ContDistErlang(model, name, order, mean, showInReport, showInTrace);
            }
            else if (dist instanceof ExponentialDistribution) {
                ExponentialDistribution expDist = (ExponentialDistribution) dist;
                double mean = expDist.getMean();
                desmojDist = new ContDistExponential(model, name, mean, showInReport, showInTrace);
            }
            else if (dist instanceof TriangularDistribution) {
                TriangularDistribution triDist = (TriangularDistribution) dist;
                double lower = triDist.getLower();
                double upper = triDist.getUpper();
                double peak = triDist.getPeak();
                desmojDist = new ContDistTriangular(model, name, lower, upper, peak, showInReport, showInTrace);
            }
            else if (dist instanceof NormalDistribution) {
                NormalDistribution norDist = (NormalDistribution) dist;
                double mean = norDist.getMean();
                double standardDeviation = norDist.getStandardDeviation();
                desmojDist = new ContDistNormal(model, name, mean, standardDeviation, showInReport, showInTrace);
            }
            else if (dist instanceof PoissonDistribution) {
                PoissonDistribution poiDist = (PoissonDistribution) dist;
                double mean = poiDist.getMean();
                desmojDist = new DiscreteDistPoisson(model, name, mean, showInReport, showInTrace);
            }
            else if (dist instanceof UniformDistribution) {
                UniformDistribution uniDist = (UniformDistribution) dist;
                double lower = uniDist.getLower();
                double upper = uniDist.getUpper();
                desmojDist = new ContDistUniform(model, name, lower, upper, showInReport, showInTrace);
            }
            else {
                throw new InstantiationException("Distribution of node " + nodeId + " not supported.");
            }

            desmojDist.setSeed(randomSeed);
            // XXX no conversion of distribution to target unit smallestTimeUnit during runtime required, desmoj does it
            // all
            distributions.put(nodeId, desmojDist);
        }
    }

    // private void convertToBranchingDistributions(Map<Integer, BranchingBehavior> branchingBehaviors,
    // Map<Integer, GatewayType> gatewayTypes) {
    // branchingDistributionsExclusive = new HashMap<Integer, DiscreteDistEmpirical<Integer>>();
    // branchingDistributionsInclusive = new HashMap<Integer, Map<Integer, BoolDistBernoulli>>();
    //
    // for (Integer nodeId : branchingBehaviors.keySet()) {
    // BranchingBehavior branchingBehavior = branchingBehaviors.get(nodeId);
    // Map<Integer, Double> branchingProbabilities = branchingBehavior.getBranchingProbabilities();
    // GatewayType type = gatewayTypes.get(nodeId);
    // String name = processModel.getModelScopeId() + "_" + nodeId.toString();
    // if (type == GatewayType.EXCLUSIVE) {
    // DiscreteDistEmpirical<Integer> desmojDist = new DiscreteDistEmpirical<Integer>(model, name,
    // showInReport, showInTrace);
    // for (Integer nextNodeId : branchingProbabilities.keySet()) {
    // Double probability = branchingProbabilities.get(nextNodeId);
    // desmojDist.addEntry(nextNodeId, probability);
    // }
    // branchingDistributionsExclusive.put(nodeId, desmojDist);
    // }
    // else if (type == GatewayType.INCLUSIVE) {
    // Map<Integer, BoolDistBernoulli> inclusiveDistributions = new HashMap<Integer, BoolDistBernoulli>();
    // for (Integer nextNodeId : branchingProbabilities.keySet()) {
    // Double probability = branchingProbabilities.get(nextNodeId);
    // BoolDistBernoulli desmojDist = new BoolDistBernoulli(model, name, probability, showInReport,
    // showInTrace);
    // inclusiveDistributions.put(nextNodeId, desmojDist);
    // }
    // branchingDistributionsInclusive.put(nodeId, inclusiveDistributions);
    // }
    // else { // is a boundaryEventDistribution
    // DiscreteDistEmpirical<Integer> desmojDist = new DiscreteDistEmpirical<Integer>(model, name,
    // showInReport, showInTrace);
    // for (Integer nextNodeId : branchingProbabilities.keySet()) {
    // Double probability = branchingProbabilities.get(nextNodeId);
    // desmojDist.addEntry(nextNodeId, probability);
    // }
    // boundaryEventDistributions.put(nodeId, desmojDist);
    // }
    // }
    // }

    public ProcessSimulationComponents getParent() {
        return parent;
    }

    public CommonProcessElements getCommonProcessElements() {
        // exists in parent only
        if (commonProcessElements != null) {
            return commonProcessElements;
        }
        return parent.getCommonProcessElements();
    }

    public void setCommonProcessElements(CommonProcessElements commonProcessElements) {
        this.commonProcessElements = commonProcessElements;
    }

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public SimulationConfiguration getSimulationConfiguration() {
        return simulationConfiguration;
    }

    public Map<Integer, ProcessSimulationComponents> getChildren() {
        return children;
    }

    public double getDistributionSample(Integer nodeId) {
        NumericalDist<?> distribution = distributions.get(nodeId);
        if (distribution == null) {
            DebugLogger.log("No distribution found for node " + nodeId + ". " + "\nUse zero time interval.");
            // distribution = new DiscreteDistConstant<Double>(model, nodeId.toString(), 0d, showInReport, showInTrace);
            return 0d;
        }
        if (distribution instanceof ContDistErlang) {
            // skip trace notes to avoid confusion
            // when order is e.g. 10, it provides 11x "samples ... from ..." notes
            // only the last one is relevant for the user, so skip first 10
            ContDistErlang dist = (ContDistErlang) distribution;
            dist.skipTraceNote((int) dist.getOrder());
        }
        return distribution.sample().doubleValue();
    }

    public Map<Integer, NumericalDist<?>> getDistributions() {
        return distributions;
    }

    public TimeUnit getDistributionTimeUnit(Integer nodeId) {
        TimeUnit distributionTimeUnit = distributionTimeUnits.get(nodeId);
        if (distributionTimeUnit == null) {
            distributionTimeUnit = TimeUnit.DAYS;
        }
        return distributionTimeUnit;
    }

    public Map<Integer, TimeUnit> getDistributionTimeUnits() {
        return distributionTimeUnits;
    }

    public double getTimeInterval(Integer nodeId) throws ScyllaRuntimeException {
        NumericalDist<?> distribution = distributions.get(nodeId);
        if (distribution == null) {
            throw new ScyllaRuntimeException("Distribution not found for node " + nodeId + ".");
        }
        return distribution.sample().doubleValue();
    }

    public int getProcessInstancesStarted() {
        return processInstancesStarted;
    }

    public SimulationModel getModel() {
        return model;
    }

    public Map<String, Map<Integer, Object>> getExtensionDistributions() {
        return extensionDistributions;
    }

    public int incrementProcessInstancesStarted() {
        return ++processInstancesStarted;
    }

}
