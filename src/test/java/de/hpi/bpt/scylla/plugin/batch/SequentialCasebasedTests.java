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
		for(List<String[]> cluster : getClusters().values()) {
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
		
		for(List<String[]> cluster : getClusters().values()) {
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
		
		Map<String, List<String[]>> resources = table.stream()
				.filter((row)->{return !row[5].isEmpty();})
				.collect(Collectors.groupingBy((each)->{return each[5];}));
		assertEquals(2, resources.size());
		for(List<String[]> activitiesPerResource : resources.values()) {
			activitiesPerResource.sort((a,b) -> {return a[4].compareTo(b[4]);});
			Deque<String> seenClusters = new LinkedList<>();
			for(String[] activity : activitiesPerResource) {
				String cluster = activity[6];
				System.err.print(cluster+" ");
				assertTrue(!seenClusters.contains(cluster) || seenClusters.removeFirst().equals(cluster));
				seenClusters.addFirst(cluster);
			}
			System.err.println();
		}
		
		for(List<String[]> cluster : getClusters().values()) {
			int numberOfResourcesPerCluster = cluster.stream()
				.filter(each -> (each[1].equals("Activity A") || each[1].equals("Activity B")))
				.collect(Collectors.groupingBy(each -> each[5])).size();
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
		Map<String, List<String[]>> activitiesPerResources = table.stream().collect(Collectors.groupingBy(each -> each[5]));
		for(Entry<String, List<String[]>> activities : activitiesPerResources.entrySet()) {
			if(activities.getKey().isEmpty())continue;
			assertNoIntersections(activities.getValue());
		}
	}
	
	public static void assertClusterIsCaseBased(List<String[]> cluster) {
		List<String[]> sortedActivities = cluster.stream()
				.filter((activity)->{return activity[1].equals("Activity A") || activity[1].equals("Activity B");})
				.sorted((a,b)->{return a[4].compareTo(b[4]);})
				.collect(Collectors.toList());
		
		Set<String> seenInstances = new HashSet<String>();
		String lastInstance = null;
		String lastEndTime = "";
		for(String[] activity : sortedActivities) {
			String instance = activity[0];
			String startTime = activity[3];
			if(!instance.equals(lastInstance)) {
				assertFalse(seenInstances.contains(instance));
				assertTrue(lastEndTime.compareTo(startTime) <= 0);
				seenInstances.add(instance);
			}
			lastInstance = instance;
			lastEndTime = activity[4].compareTo(lastEndTime) > 0 ? activity[4] : lastEndTime;
		}
	}

}
