package de.hpi.bpt.scylla.plugin.batch;


import static org.junit.jupiter.api.Assertions.*;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.hpi.bpt.scylla.TestSeeds;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.plugin.batch.BatchCSVLogger.BatchCSVEntry;
import de.hpi.bpt.scylla.plugin.statslogger_nojar.StatisticsLogger;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_type.logger.OutputLoggerPluggable;


public class SequentialCasebasedTests extends BatchSimulationTest{
	
	{
		executionType = BatchClusterExecutionType.SEQUENTIAL_CASEBASED;
	}
	
	public static void main(String[] args) {
		PluginLoader.getDefaultPluginLoader().getExtensions().get(OutputLoggerPluggable.class).stream().filter(each -> each.equals(StatisticsLogger.class)).findAny().get().stateChanged(false);
		DebugLogger.allowDebugLogging = false;
		SequentialCasebasedTests x = new SequentialCasebasedTests();
		x.testResourceStable(8619657395064501318L);
	}
	
	@ParameterizedTest
	@CsvSource({
		"ModelSimple.bpmn, BatchTestSimulationConfiguration.xml",
		"ModelBatchTask.bpmn, BatchTestSimulationConfigurationBatchTask.xml"
	})
	public void testCasesAreSequential(String model, String config) {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				model,
				config);
		
		assertEquals(30, table.size());
		assertExecutionType();
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			assertClusterIsCaseBased(cluster);
		}
	}
	
	@Test
	public void testParallelGateway() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayParallel.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		assertEquals(30, table.size());
		assertExecutionType();
		
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			assertClusterIsCaseBased(cluster);
		}
	}

	@TestSeeds({-3207906028196791040L, 8619657395064501318L, 3525936635746277536L})
	public void testResourceStable(long seed) {
		setGlobalSeed(seed);
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayParallel.bpmn", 
				"BatchTestSimulationConfigurationWithResources.xml");
		
		assertEquals(30, table.size());
		
		Map<String, List<BatchCSVEntry>> resources = table.stream()
				.filter((row)->{return !row.getResources().isEmpty();})
				.collect(Collectors.groupingBy(BatchCSVEntry::getResources));
		assertEquals(2, resources.size());
		for(List<BatchCSVEntry> activitiesPerResource : resources.values()) {
			activitiesPerResource.sort((a,b) -> {return a.getComplete().compareTo(b.getComplete());});
			Deque<String> seenClusters = new LinkedList<>();
			for(BatchCSVEntry activity : activitiesPerResource) {
				String cluster = activity.getBatchNumber();
				System.err.print(cluster+" ");
				assertTrue(!seenClusters.contains(cluster) || seenClusters.removeFirst().equals(cluster));
				seenClusters.addFirst(cluster);
			}
			System.err.println();
		}
		
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			int numberOfResourcesPerCluster = cluster.stream()
				.filter(each -> (each.getActivityName().equals("Activity A") || each.getActivityName().equals("Activity B")))
				.collect(Collectors.groupingBy(BatchCSVEntry::getResources)).size();
			assertEquals(1, numberOfResourcesPerCluster);
		}
	}
	
	@Test
	public void testEventScheduledTwiceRegression() {
		runSimpleSimulation(
				"regression\\DoubleEventGlobal.xml", 
				"regression\\DoubleEvent.bpmn", 
				"regression\\DoubleEventSim.xml");
	}
	
	@TestSeeds(-3923947980161818345L)
	public void testDoubleResourceRegression(long seed) {
		setGlobalSeed(seed);
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayParallel.bpmn", 
				"BatchTestSimulationConfigurationWithResources.xml");
		Map<String, List<BatchCSVEntry>> activitiesPerResources = table.stream().collect(Collectors.groupingBy(BatchCSVEntry::getResources));
		for(Entry<String, List<BatchCSVEntry>> activities : activitiesPerResources.entrySet()) {
			if(activities.getKey().isEmpty())continue;
			assertNoIntersections(activities.getValue());
		}
	}
	
	public static void assertClusterIsCaseBased(List<BatchCSVEntry> cluster) {
		List<BatchCSVEntry> sortedActivities = cluster.stream()
				.filter((activity)->{return activity.getActivityName().equals("Activity A") || activity.getActivityName().equals("Activity B");})
				.sorted((a,b)->{return a.getComplete().compareTo(b.getComplete());})
				.collect(Collectors.toList());
		
		Set<Integer> seenInstances = new HashSet<Integer>();
		Integer lastInstance = null;
		String lastEndTime = "";
		for(BatchCSVEntry activity : sortedActivities) {
			Integer instance = activity.getInstanceId();
			String startTime = activity.getArrival();
			if(!instance.equals(lastInstance)) {
				assertFalse(seenInstances.contains(instance));
				assertTrue(lastEndTime.compareTo(startTime) <= 0);
				seenInstances.add(instance);
			}
			lastInstance = instance;
			lastEndTime = activity.getComplete().compareTo(lastEndTime) > 0 ? activity.getComplete() : lastEndTime;
		}
	}

}
