package de.hpi.bpt.scylla.plugin.batch;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.javatuples.Pair;
import org.jdom2.JDOMException;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import de.hpi.bpt.scylla.TestSeeds;
import de.hpi.bpt.scylla.TestUtils;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.plugin.batch.BatchCSVLogger.BatchCSVEntry;
import de.hpi.bpt.scylla.plugin.dataobject.DataObjectField;

public class MinMaxRuleTests extends BatchSimulationTest {
	
	
	
	public static void main(String[] args) {
		new MinMaxRuleTests().testExpectedTimeoutAndLoad(-7969201103926881908L);
//		new MinMaxRuleTests().testMinInstancesIsUsed(806123754304894459L);
//		new MinMaxRuleTests().testMaxInstancesIsUsed(1612247508609788918L);
	}
	
	@Test
	public void testBasicWillReach() {
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelWithGrouping.bpmn", 
				"BatchTestSimulationConfigurationWithDataObject.xml");
		try {
			simulationManager._parseInput();
			assertTrue(MinMaxRule.willReach(
					getProcessModel(), 
					Stream.of(getProcessModel().getIdentifiersToNodeIds().get("Task_A"), getProcessModel().getIdentifiersToNodeIds().get("Task_B")).collect(Collectors.toSet()), 
					getProcessModel().getIdentifiersToNodeIds().get("BatchTask")));

			assertFalse(MinMaxRule.willReach(
					getProcessModel(), 
					Stream.of(getProcessModel().getIdentifiersToNodeIds().get("Task_B")).collect(Collectors.toSet()), 
					getProcessModel().getIdentifiersToNodeIds().get("BatchTask")));
			

			assertFalse(MinMaxRule.willReach(
					getProcessModel(), 
					Stream.of(getProcessModel().getIdentifiersToNodeIds().get("BatchTask")).collect(Collectors.toSet()), 
					getProcessModel().getIdentifiersToNodeIds().get("BatchTask")));
		} catch (ScyllaValidationException | JDOMException | IOException e) {
			e.printStackTrace();
			fail(e);
		}
	}
	
	@TestSeeds({-1633284822204608005L, 806123754304894459L, -7860141510320648011L, -7969201103926881908L})
	public void testExpectedTimeoutAndLoad(long seed) {
		setGlobalSeed(seed);
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelWithGrouping.bpmn", 
				"BatchTestSimulationConfigurationWithDataObject.xml");
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			Integer numberOfInstances = cluster.size();
			Duration activationTime = Duration.between(firstEnableOf(cluster).toInstant(), startOf(cluster).toInstant());
			Pair<Integer, Duration> expectedLoadAndActivationTime = expectedLoadAndActivationTime(cluster);
			assertEquals(expectedLoadAndActivationTime, Pair.with(numberOfInstances, activationTime), "Cluster "+cluster.get(0).getBatchNumber());
		}
	}


	private Pair<Integer, Duration> expectedLoadAndActivationTime(List<BatchCSVEntry> cluster) {
		Pair<Integer, Duration> expected = Pair.with(-1, Duration.ofSeconds(0));
		int currentLoad = 0;
		Duration currentDuration = Duration.ofSeconds(0);
		Date firstEnableOfCluster = firstEnableOf(cluster);
		for(Date arrivalOfNewInstance : cluster.stream()
			.map(BatchCSVEntry::getArrival)
			.map(t -> {try {return BatchCSVLogger.timeFormat.parse(t);} catch (ParseException e) {fail(e); return null;}})
			.sorted()
			.collect(Collectors.toList())) 
		{
			currentLoad++;
			currentDuration = Duration.between(firstEnableOfCluster.toInstant(), arrivalOfNewInstance.toInstant());
			if(similarInstancesWereAvailableAt(arrivalOfNewInstance, cluster)) {
				if(currentLoad >= getMaxInstances() || currentDuration.compareTo(getMaxTimeOut()) >= 0)return Pair.with(currentLoad, currentDuration);
				expected = Pair.with(currentLoad, getMaxTimeOut());
			} else {
				if(currentLoad >= getMinInstances() || currentDuration.compareTo(getMinTimeOut()) >= 0)return Pair.with(currentLoad, currentDuration);
				expected = Pair.with(currentLoad, getMinTimeOut());
			}
		}
		return expected;
	}
	

	//@TestSeeds({-1633284822204608005L, 806123754304894459L})
	public void testMinInstancesIsUsed(long seed) {
		setGlobalSeed(seed);
		boolean hasAsserted = false;
		while(!hasAsserted) {
			runSimpleSimulation(
					"BatchTestGlobalConfiguration.xml", 
					"ModelWithGrouping.bpmn", 
					"BatchTestSimulationConfigurationWithDataObject.xml");
			for(List<BatchCSVEntry> cluster : getClusters().values()) {
				Date firstEnableOfCluster = firstEnableOf(cluster);
				if(!similarInstancesWereAvailableAt(firstEnableOfCluster, cluster)) {
					hasAsserted = true;
					int numberOfInstances = cluster.size();
					Duration activationTime = Duration.between(firstEnableOfCluster.toInstant(), startOf(cluster).toInstant());
					
					assertTrue(
							getMinInstances().equals(numberOfInstances) ||
							getMinTimeOut().equals(activationTime));
				};
			}
		}
	}
	
	//@TestSeeds({1612247508609788918L, 806123754304894459L})
	public void testMaxInstancesIsUsed(long seed) {
		setGlobalSeed(seed);
		boolean hasAsserted = false;
		int remainingRetries = 10;
		while(!hasAsserted) {
			runSimpleSimulation(
					"BatchTestGlobalConfiguration.xml", 
					"ModelWithGrouping.bpmn", 
					"BatchTestSimulationConfigurationWithDataObject.xml");
			for(List<BatchCSVEntry> cluster : getClusters().values()) {
				Date firstEnableOfCluster = firstEnableOf(cluster);
				if(similarInstancesWereAvailableAt(firstEnableOfCluster, cluster)) {
					hasAsserted = true;
					int numberOfInstances = cluster.size();
					Duration activationTime = Duration.between(firstEnableOfCluster.toInstant(), startOf(cluster).toInstant());
					
					assertTrue(
						getMaxInstances().equals(numberOfInstances) ||
						getMaxTimeOut().equals(activationTime),
						"Cluster "+cluster.get(0).getBatchNumber()+" contained "+numberOfInstances+" instances and took "+activationTime+" to start.");
				};
			}
			if(remainingRetries <= 0)throw new AssertionFailedError("No simulation run showed signs of a mexInstance case");
			beforeParsingGlobal.add(()->setGlobalSeed(seed*2));
			remainingRetries--;		
			System.err.println();
		}
	}
	
	private Map<Object, List<Pair<Date, Date>>> availableDataObjects() {
		//[ObjectValue:TimeWhenAnyProcessInstanceWithThatValueWasAvailable]
		Map<Object, List<Pair<Date, Date>>> instanceWithDataObjectAvailabilities = new HashMap<>();
		table.stream().filter(each -> each.getActivityName().equals("Activity A")).forEach(entry -> {
			try {
				Date arrival = BatchCSVLogger.timeFormat.parse(entry.getArrival());
				Date complete = BatchCSVLogger.timeFormat.parse(entry.getComplete());
				Object dataObjectValue = DataObjectField.getDataObjectValue(entry.getInstanceId(),"DataObject.Value");
				instanceWithDataObjectAvailabilities
					.computeIfAbsent(dataObjectValue, x -> new ArrayList<>())
					.add(new Pair<Date, Date>(arrival, complete));
			} catch (ParseException e) {
				e.printStackTrace();
				fail(e);
			}
		});
		return instanceWithDataObjectAvailabilities;
	}
	
	@Override
	protected void afterParsing() {
		super.afterParsing();
		TestUtils.setAttribute(getBatchActivity(), "activationRule", new MinMaxRule(2, Duration.ofMinutes(20), 4, Duration.ofMinutes(60)));
		TestUtils.setAttribute(getSimulationConfiguration(), "numberOfProcessInstances", 20);
	}
	
	protected MinMaxRule getActivationRule() {
		return (MinMaxRule) getBatchActivity().getActivationRule();
	}
	
	protected Integer getMaxInstances() {
		return (Integer) TestUtils.getAttribute(getActivationRule(), "maxInstances");
	}	
	
	protected Integer getMinInstances() {
		return (Integer) TestUtils.getAttribute(getActivationRule(), "minInstances");
	}
	
	protected Duration getMinTimeOut() {
		return (Duration) TestUtils.getAttribute(getActivationRule(), "minTimeout");
	}
	
	protected Duration getMaxTimeOut() {
		return (Duration) TestUtils.getAttribute(getActivationRule(), "maxTimeout");
	}
	
	protected boolean similarInstancesWereAvailableAt(Date aPointInTime, List<BatchCSVEntry> cluster) {
		Object dataObjectValue = DataObjectField.getDataObjectValue(cluster.get(0).getInstanceId(),"DataObject.Value");
		return availableDataObjects().get(dataObjectValue).stream()
				.filter(each -> (aPointInTime.compareTo(each.getValue0()) >= 0 && aPointInTime.compareTo(each.getValue1()) <= 0))
				.count() > 1;
	}

	protected static Date firstEnableOf(List<BatchCSVEntry> cluster) {
		return cluster.stream()
			.map(BatchCSVEntry::getArrival)
			.map(t -> {try {return BatchCSVLogger.timeFormat.parse(t);} catch (ParseException e) {fail(e); return null;}})
			.min(Date::compareTo).get();
	}
	
	protected static Date startOf(List<BatchCSVEntry> cluster) {
		return cluster.stream()
			.map(BatchCSVEntry::getStart)
			.map(t -> {try {return BatchCSVLogger.timeFormat.parse(t);} catch (ParseException e) {fail(e); return null;}})
			.min(Date::compareTo).get();
	}

}
