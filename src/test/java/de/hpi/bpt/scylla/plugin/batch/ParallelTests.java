package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.hpi.bpt.scylla.plugin.batch.BatchCSVLogger.BatchCSVEntry;

public class ParallelTests extends BatchSimulationTest{
	
	{
		executionType = BatchClusterExecutionType.PARALLEL;
	}
	
	public static void main(String[] args) {
		ParallelTests x = new ParallelTests();
		//x.runSimpleSimulation("BatchTestGlobalConfiguration.xml", "ModelSimple.bpmn", "BatchTestSimulationConfiguration.xml");
		x.testCasesAreParallelWithResources("ModelBatchTask.bpmn", "BatchTestSimulationConfigurationBatchTaskWithResources.xml");
		//x.testCasesAreParallel("ModelSimple.bpmn", "BatchTestSimulationConfiguration.xml");
	}
	
	@ParameterizedTest
	@CsvSource({
		"ModelSimple.bpmn, BatchTestSimulationConfiguration.xml",
		"ModelBatchTask.bpmn, BatchTestSimulationConfigurationBatchTask.xml"
	})
	public void testCasesAreParallel(String model, String config) {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				model,
				config);
		
		assertEquals(30, table.size());
		assertExecutionType();
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			assertActivityGroupsAreEqual(cluster);
		}
	}
	
	@ParameterizedTest
	@CsvSource({
		"ModelSimple.bpmn, BatchTestSimulationConfigurationWithResources.xml",
		//TODO "ModelBatchTask.bpmn, BatchTestSimulationConfigurationBatchTaskWithResources.xml"
	})
	public void testCasesAreParallelWithResources(String model, String config) {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				model,
				config);
		
		assertEquals(30, table.size());
		assertExecutionType();
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			assertActivityGroupsAreEqual(cluster);
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
		assertNoNaturalArrivalTime(table);
		
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			assertActivityGroupsAreEqual(cluster);
		}
	}
	
	public static void assertActivityGroupsAreEqual(List<BatchCSVEntry> cluster) {
		Map<String, List<BatchCSVEntry>> activityGroups = cluster.stream()
				.collect(Collectors.groupingBy(BatchCSVEntry::getActivityName));
		//For each activity group assert that all information except process instance and arrival time are equal
		for(List<BatchCSVEntry> activityGroup : activityGroups.values()) {
			Object[] first = activityGroup.get(0).toArray();
			for(BatchCSVEntry activity : activityGroup) {
				for(int i = 0; i < first.length; i++) {
					if(i == 0 || i == 2)continue;
					assertEquals(first[i],activity.toArray()[i]);
				}
			};
		}
	}
	
	private static void assertNoNaturalArrivalTime(List<BatchCSVEntry> table) {
		table.stream().forEach((each)->{assertTrue(each.getNaturalArrival().isEmpty());});
	}
	

}
