package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.JDOMException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.hpi.bpt.scylla.TestUtils;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;

public class CSVLoggerTests extends BatchSimulationTest{
	
	public static void main(String[] args) {
		try {
			new CSVLoggerTests().testLogIsCreated("ModelBatchTask.bpmn", "BatchTestSimulationConfigurationBatchTask.xml");
			//new CSVLoggerTests().testLogIsCreated("ModelSimple.bpmn", "BatchTestSimulationConfiguration.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@ParameterizedTest
	@CsvSource({
		"ModelSimple.bpmn, BatchTestSimulationConfiguration.xml",
		"ModelBatchTask.bpmn, BatchTestSimulationConfigurationBatchTask.xml"
	})
	public void testLogIsCreated(String model, String config) throws ScyllaValidationException, JDOMException, IOException {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				model,
				config);
		
		assertNotNull(table);
	}
	
	
	@Test
	public void testHeaderLength() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		
		assertEquals(BatchCSVLogger.headerLength(), table.get(0).length);		
	}
	
	@Test
	public void testNaturalEnablementTaskbased() {
		executionType = BatchClusterExecutionType.SEQUENTIAL_TASKBASED;
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		
		for(List<String[]> cluster : getClusters().values()) {
			Map<String, List<String[]>> activities = TestUtils.groupBy(cluster, 1);
			assertInstanceWiseNaturalEnablement(activities.get("Batch Activity"), activities.get("Activity A"));
			assertConstantNaturalEnablement(activities.get("Activity B"));
		}
		
	}
	
	@Test
	public void testNaturalEnablementCasebased() {
		executionType = BatchClusterExecutionType.SEQUENTIAL_CASEBASED;
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		
		for(List<String[]> cluster : getClusters().values()) {
			Map<String, List<String[]>> activities = TestUtils.groupBy(cluster, 1);
			assertInstanceWiseNaturalEnablement(activities.get("Batch Activity"), activities.get("Activity A"));
		}
		
	}
	
	@Test
	public void testUnwantedActivityInBatchRegression() {
		runSimpleSimulation(
				"regression\\XORSamplesGlobal.xml", 
				"regression\\UnwantedActivityInBatch.bpmn", 
				"regression\\UnwantedActivityInBatch.xml");
		Map<String, List<String[]>> activities = table.stream().collect(Collectors.groupingBy(each -> each[1]));
		activities.get("A").stream().forEach(each -> assertTrue(each[7].isEmpty(), each.toString()));
		activities.get("E").stream().forEach(each -> assertTrue(each[7].isEmpty(), each.toString()));
		activities.get("B").stream().forEach(each -> assertFalse(each[7].isEmpty(), each.toString()));
		activities.get("C").stream().forEach(each -> assertFalse(each[7].isEmpty(), each.toString()));
		activities.get("D").stream().forEach(each -> assertFalse(each[7].isEmpty(), each.toString()));
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
