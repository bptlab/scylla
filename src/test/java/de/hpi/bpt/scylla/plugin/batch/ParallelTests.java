package de.hpi.bpt.scylla.plugin.batch;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import de.hpi.bpt.scylla.TestUtils;

public class ParallelTests extends BatchSimulationTest{
	
	{
		executionType = BatchClusterExecutionType.PARALLEL;
	}
	
	public static void main(String[] args) {
		ParallelTests x = new ParallelTests();
		x.testParallelGateway();
	}

	
	@Test
	public void testParallelGateway() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayParallel.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		
		Assert.assertEquals(30, table.size());
		assertExecutionType(table);
		assertNoNaturalArrivalTime(table);
		Map<String, List<String[]>> clusters = TestUtils.groupByCluster(table);
		
		for(List<String[]> cluster : clusters.values()) {
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
					Assert.assertEquals(first[i],activity[i]);
				}
			};
		}
	}
	
	private static void assertNoNaturalArrivalTime(List<String[]> table) {
		table.stream().forEach((each)->{Assert.assertTrue(each[8].isEmpty());});
	}
	
	private static void assertExecutionType(List<String[]> table) {
		table.stream().forEach((each)->{Assert.assertEquals(BatchClusterExecutionType.PARALLEL.toString(), each[7]);});
	}

}
