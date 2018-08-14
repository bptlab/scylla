package de.hpi.bpt.scylla.plugin.batch;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import de.hpi.bpt.scylla.TestUtils;

public class SequentialTaskbasedTests extends SimulationTest{
	
	public static void main(String[] args) {
		SequentialTaskbasedTests x = new SequentialTaskbasedTests();
		//x.testActivitiesAreSequential();
		x.testParallelGateway();
	}
	
	protected String getFolder() {return "src\\test\\resources\\BatchPlugin\\";}
	
	@Test(timeout=3000)
	public void testActivitiesAreSequential() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"TaskbasedModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		File f = new File(".\\"+outputPath+"Process_1_processBatchActivityStats.csv");
		Assert.assertTrue(f.exists());
		List<String[]> table = TestUtils.readCSV(f);
		
		Assert.assertEquals(30, table.size());
		Map<String, List<String[]>> clusters = TestUtils.orderByCluster(table);
		for(List<String[]> cluster : clusters.values()) {
			assertActivityIsSequential("Activity A", cluster);
			assertActivityIsSequential("Activity B", cluster);
			assertActivitiesDoNotIntersect("Activity A", "Activity B", cluster);
		}
	}
	
	@Test
	public void testParallelGateway() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"TaskbasedModelGatewayParallel.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		File f = new File(".\\"+outputPath+"Process_1_processBatchActivityStats.csv");
		Assert.assertTrue(f.exists());
		List<String[]> table = TestUtils.readCSV(f);
		
		Assert.assertEquals(30, table.size());
		assertActivityIsInEveryInstance("Activity A", table);
		assertActivityIsInEveryInstance("Activity B", table);
		
		Map<String, List<String[]>> clusters = TestUtils.orderByCluster(table);
		for(List<String[]> cluster : clusters.values()) {
			assertActivityIsSequential("Activity A", cluster);
			assertActivityIsSequential("Activity B", cluster);
		}
	}
	
	private static void assertActivityIsSequential(String activityName, List<String[]> rows) {
		rows = rows.stream()
				.filter((String[] row)->{return row[1].equals(activityName);})
				.sorted((a,b) -> {return a[3].compareTo(b[3]);})
				.collect(Collectors.toList());
		for(int i = 0; i < rows.size()-1; i++) {
			String[] current = rows.get(i);
			String[] next = rows.get(i+1);
			Assert.assertTrue(current[4].compareTo(next[3]) <= 0);
		}
	}
	
	private static void assertActivitiesDoNotIntersect(String activityNameA, String activityNameB, List<String[]> cluster) {
		Map<Object, List<String[]>> e = cluster.stream()
				.collect(Collectors.groupingBy((row)->{return row[1];}));
			String[] lastActivityA = e.get(activityNameA).stream().max((a,b) -> {return a[4].compareTo(b[4]);}).get();
			String[] firstActivityB = e.get(activityNameB).stream().min((a,b) -> {return a[3].compareTo(b[3]);}).get();
			Assert.assertTrue(lastActivityA[4].compareTo(firstActivityB[3]) <= 0);
	}
	
	private void assertActivityIsInEveryInstance(String activityName, List<String[]> table) {
		for(int i = 1; i <= numberOfInstances(); i++) {
			final int j = i;
			Assert.assertTrue(table.stream().anyMatch((row) -> {
				return row[0].equals(""+j) && row[1].equals(activityName);
			}));
		}
	}

}
