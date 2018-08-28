package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.hpi.bpt.scylla.SimulationTest;
import de.hpi.bpt.scylla.TestUtils;

public class CSVLoggerTests extends SimulationTest{

	@Override
	protected String getFolder() {return "src\\test\\resources\\BatchPlugin\\";}
	
	@Test
	public void testNaturalEnablementTaskbased() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"TaskbasedModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		File f = new File(".\\"+outputPath+"Process_1_processBatchActivityStats.csv");
		assertTrue(f.exists());
		List<String[]> table = TestUtils.readCSV(f);
		assert table.get(0).length == BatchCSVLogger.headerLength();
		Map<String, List<String[]>> clusters = TestUtils.groupByCluster(table);
		for(List<String[]> cluster : clusters.values()) {
			Map<String, List<String[]>> activities = TestUtils.groupBy(cluster, 1);
			assertInstanceWiseNaturalEnablement(activities.get("Batch Activity"), activities.get("Activity A"));
			assertConstantNaturalEnablement(activities.get("Activity B"));
		}
		
	}
	
	@Test
	public void testNaturalEnablementCasebased() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"CasebasedModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		File f = new File(".\\"+outputPath+"Process_1_processBatchActivityStats.csv");
		assertTrue(f.exists());
		List<String[]> table = TestUtils.readCSV(f);
		
		Map<String, List<String[]>> clusters = TestUtils.groupByCluster(table);
		for(List<String[]> cluster : clusters.values()) {
			Map<String, List<String[]>> activities = TestUtils.groupBy(cluster, 1);
			assertInstanceWiseNaturalEnablement(activities.get("Batch Activity"), activities.get("Activity A"));
		}
		
	}
	
	private static void assertConstantNaturalEnablement(List<String[]> activity) {
		for(String[] instance : activity) {
			assertEquals(activity.get(0)[8],instance[8]);
		}
	}
	
	private static void assertInstanceWiseNaturalEnablement(List<String[]> batchActivity, List<String[]> firstTask) {
		assert batchActivity.size() == firstTask.size();
		batchActivity.sort((first,second)->{return first[0].compareTo(second[0]);});
		firstTask.sort((first,second)->{return first[0].compareTo(second[0]);});
		for(int i = 0; i < batchActivity.size(); i++) {
			assertEquals(batchActivity.get(i)[2],firstTask.get(i)[8]);
		}
	}
}
