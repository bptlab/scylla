package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
import de.hpi.bpt.scylla.plugin.batch.BatchCSVLogger.BatchCSVEntry;

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
		
		assertEquals(BatchCSVLogger.headerLength(), table.get(0).toArray().length);		
	}
	
	@Test
	public void testNaturalEnablementTaskbased() {
		executionType = BatchClusterExecutionType.SEQUENTIAL_TASKBASED;
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			Map<String, List<BatchCSVEntry>> activities = TestUtils.groupBy(cluster, BatchCSVEntry::getActivityName);
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
		
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			Map<String, List<BatchCSVEntry>> activities = TestUtils.groupBy(cluster, BatchCSVEntry::getActivityName);
			assertInstanceWiseNaturalEnablement(activities.get("Batch Activity"), activities.get("Activity A"));
		}
		
	}
	
	@Test
	public void testUnwantedActivityInBatchRegression() {
		runSimpleSimulation(
				"regression/XORSamplesGlobal.xml", 
				"regression/UnwantedActivityInBatch.bpmn", 
				"regression/UnwantedActivityInBatch.xml");
		Map<String, List<BatchCSVEntry>> activities = table.stream().collect(Collectors.groupingBy(BatchCSVEntry::getActivityName));
		activities.get("A").stream().forEach(each -> assertNull(each.getBatchType(), each.toString()));
		activities.get("E").stream().forEach(each -> assertNull(each.getBatchType(), each.toString()));
		activities.get("B").stream().forEach(each -> assertNotNull(each.getBatchType(), each.toString()));
		activities.get("C").stream().forEach(each -> assertNotNull(each.getBatchType(), each.toString()));
		activities.get("D").stream().forEach(each -> assertNotNull(each.getBatchType(), each.toString()));
	}
	
	private static void assertConstantNaturalEnablement(List<BatchCSVEntry> activity) {
		for(BatchCSVEntry instance : activity) {
			assertEquals(activity.get(0).getNaturalArrival(), instance.getNaturalArrival());
		}
	}
	
	private static void assertInstanceWiseNaturalEnablement(List<BatchCSVEntry> batchActivity, List<BatchCSVEntry> firstTask) {
		assert batchActivity.size() == firstTask.size();
		batchActivity.sort((first,second) -> {return first.getInstanceId().compareTo(second.getInstanceId());});
		firstTask.sort((first,second) -> {return first.getInstanceId().compareTo(second.getInstanceId());});
		for(int i = 0; i < batchActivity.size(); i++) {
			assertEquals(batchActivity.get(i).getArrival(), firstTask.get(i).getNaturalArrival());
		}
	}
}
