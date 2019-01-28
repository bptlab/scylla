package de.hpi.bpt.scylla.plugin.batch;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.hpi.bpt.scylla.TestSeeds;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.plugin.batch.BatchCSVLogger.BatchCSVEntry;

public class SequentialTaskbasedTests extends BatchSimulationTest{
	
	{
		executionType = BatchClusterExecutionType.SEQUENTIAL_TASKBASED;
	}
	
	public static void main(String[] args) {
		DebugLogger.allowDebugLogging = false;
		SequentialTaskbasedTests x = new SequentialTaskbasedTests();
		//x.testActivitiesAreSequential();
		//x.testParallelGateway();
		//x.testResourceStable(-10L);
		x.runSimpleSimulation("BatchTestGlobalConfiguration.xml", "ModelBatchTask.bpmn", "BatchTestSimulationConfigurationBatchTaskWithResources.xml");
		//x.runSimpleSimulation("BatchTestGlobalConfiguration.xml", "ModelSimple.bpmn", "BatchTestSimulationConfigurationWithResources.xml");
	}
	
	@ParameterizedTest
	@CsvSource({
		"ModelSimple.bpmn, BatchTestSimulationConfiguration.xml",
		"ModelBatchTask.bpmn, BatchTestSimulationConfigurationBatchTask.xml"
	})
	public void testActivitiesAreSequential(String model, String config) {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				model,
				config);
		
		assertEquals(30, table.size());
		assertExecutionType();
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			assertActivityIsSequential("Activity A", cluster);
			assertActivityIsSequential("Activity B", cluster);
		}
	}
	
	@ParameterizedTest
	@CsvSource({
		"ModelSimple.bpmn, BatchTestSimulationConfigurationWithResources.xml",
		//"ModelBatchTask.bpmn, BatchTestSimulationConfigurationBatchTaskWithResources.xml"
	})
	public void testActivitiesAreSequentialWithResources(String model, String config) {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				model,
				config);
		
		assertEquals(30, table.size());
		assertExecutionType();
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			assertActivityIsSequential("Activity A", cluster);
			assertActivityIsSequential("Activity B", cluster);
		}
	}
	
	@Test
	public void testActivitiesDoNotIntersect() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			assertActivitiesDoNotIntersect("Activity A", "Activity B", cluster);
		}
	}
	
	@Test
	public void testParallelGateway() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayParallel.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		
		assertEquals(30, table.size());
		assertActivityIsInEveryInstance("Activity A", table);
		assertActivityIsInEveryInstance("Activity B", table);
		
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			assertActivityIsSequential("Activity A", cluster);
			assertActivityIsSequential("Activity B", cluster);
		}
	}


	@TestSeeds(-3207906028196791040L)
	public void testResourceStable(Long seed) {
		setGlobalSeed(seed);
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayParallel.bpmn", 
				"BatchTestSimulationConfigurationWithResources.xml");
		
		assert getGlobalConfiguration().getRandomSeed().equals(seed);
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
	}
	
	private static void assertActivityIsSequential(String activityName, List<BatchCSVEntry> rows) {
		rows = rows.stream()
				.filter(row -> {return row.getActivityName().equals(activityName);})
				.sorted((a,b) -> {return a.getStart().compareTo(b.getStart());})
				.collect(Collectors.toList());
		for(int i = 0; i < rows.size()-1; i++) {
			BatchCSVEntry current = rows.get(i);
			BatchCSVEntry next = rows.get(i+1);
			assertTrue(current.getComplete().compareTo(next.getStart()) <= 0);
		}
	}
	
	private void assertActivityIsInEveryInstance(String activityName, List<BatchCSVEntry> table) {
		for(int i = 1; i <= numberOfInstances(); i++) {
			final int j = i;
			assertTrue(table.stream().anyMatch((row) -> {
				return row.getInstanceId().equals(j) && row.getActivityName().equals(activityName);
			}));
		}
	}

	private static final void assertActivitiesDoNotIntersect(String activityNameA, String activityNameB, List<BatchCSVEntry> cluster) {
		Map<Object, List<BatchCSVEntry>> e = cluster.stream()
				.collect(Collectors.groupingBy(BatchCSVEntry::getActivityName));
			BatchCSVEntry lastActivityA = e.get(activityNameA).stream().max((a,b) -> {return a.getComplete().compareTo(b.getComplete());}).get();
			BatchCSVEntry firstActivityB = e.get(activityNameB).stream().min((a,b) -> {return a.getStart().compareTo(b.getStart());}).get();
			assertTrue(lastActivityA.getComplete().compareTo(firstActivityB.getStart()) <= 0, Arrays.toString(lastActivityA.toArray())+" "+Arrays.toString(firstActivityB.toArray()));
	}
}
