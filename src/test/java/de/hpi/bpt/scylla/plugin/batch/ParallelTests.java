package de.hpi.bpt.scylla.plugin.batch;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import de.hpi.bpt.scylla.TestUtils;

public class ParallelTests extends SimulationTest{
	
	public static void main(String[] args) {
		ParallelTests x = new ParallelTests();
		x.testParallelGateway();
	}
	
	protected String getFolder() {return "src\\test\\resources\\BatchPlugin\\";}
	
	@Test
	public void testParallelGateway() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ParallelModelGatewayParallel.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		File f = new File(".\\"+outputPath+"Process_1_processBatchActivityStats.csv");
		Assert.assertTrue(f.exists());
		
		List<String[]> table = TestUtils.readCSV(f);
		Assert.assertEquals(30, table.size());
		Map<String, List<String[]>> clusters = TestUtils.groupByCluster(table);
		
		for(List<String[]> cluster : clusters.values()) {
			assertActivityGroupsAreEqual(cluster);
		}
	}
	
	private static void assertActivityGroupsAreEqual(List<String[]> cluster) {
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

}
