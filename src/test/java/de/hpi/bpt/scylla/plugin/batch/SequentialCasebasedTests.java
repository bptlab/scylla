package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class SequentialCasebasedTests extends BatchSimulationTest{
	
	{
		executionType = BatchClusterExecutionType.SEQUENTIAL_CASEBASED;
	}
	
	public static void main(String[] args) {
		SequentialCasebasedTests x = new SequentialCasebasedTests();
		x.testEventScheduledDoubleRegression();
	}
	
	@Test
	public void testParallelGateway() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayParallel.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		Assert.assertEquals(30, table.size());
		assertExecutionType();
		
		for(List<String[]> cluster : getClusters().values()) {
			assertClusterIsCaseBased(cluster);
		}
	}
	
	@Test
	public void testResourceStable() {
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
				.collect(Collectors.groupingBy(each -> each[6])).size();
			assertEquals(1, numberOfResourcesPerCluster);
		}
	}
	
	@Test
	public void testEventScheduledDoubleRegression() {
		runSimpleSimulation(
				"regression\\DoubleEventGlobal.xml", 
				"regression\\DoubleEvent.bpmn", 
				"regression\\DoubleEventSim.xml");
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
				Assert.assertFalse(seenInstances.contains(instance));
				Assert.assertTrue(lastEndTime.compareTo(startTime) <= 0);
				seenInstances.add(instance);
			}
			lastInstance = instance;
			lastEndTime = activity[4].compareTo(lastEndTime) > 0 ? activity[4] : lastEndTime;
		}
	}

}
