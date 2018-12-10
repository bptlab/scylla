package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ParallelTests extends BatchSimulationTest{
	
	{
		executionType = BatchClusterExecutionType.PARALLEL;
	}
	
	public static void main(String[] args) {
		ParallelTests x = new ParallelTests();
		x.testCasesAreParallel("ModelBatchTask.bpmn", "BatchTestSimulationConfigurationBatchTask.xml");
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
		for(List<String[]> cluster : getClusters().values()) {
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
		
		for(List<String[]> cluster : getClusters().values()) {
			assertActivityGroupsAreEqual(cluster);
		}
	}
	
	public static void assertActivityGroupsAreEqual(List<String[]> cluster) {
		Map<String, List<String[]>> activityGroups = cluster.stream()
				.collect(Collectors.groupingBy((activity)->{return activity[1];}));
		//For each activity group assert that all information except process instance and arrival time are equal
		for(List<String[]> activityGroup : activityGroups.values()) {
			String[] first = activityGroup.get(0);
			for(String[] activity : activityGroup) {
				for(int i = 0; i < activity.length; i++) {
					if(i == 0 || i == 2)continue;
					assertEquals(first[i],activity[i]);
				}
			};
		}
	}
	
	private static void assertNoNaturalArrivalTime(List<String[]> table) {
		table.stream().forEach((each)->{assertTrue(each[8].isEmpty());});
	}
	

}
