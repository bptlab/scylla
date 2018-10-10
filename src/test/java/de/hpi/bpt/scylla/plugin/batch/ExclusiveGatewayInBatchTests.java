package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.hpi.bpt.scylla.SimulationTest;
import de.hpi.bpt.scylla.TestUtils;
import de.hpi.bpt.scylla.logger.DebugLogger;

public class ExclusiveGatewayInBatchTests extends BatchSimulationTest{
	
	private BatchClusterExecutionType executionType;
	
	public static void main(String[] args) {
		DebugLogger.allowDebugLogging = false;
		ExclusiveGatewayInBatchTests x = new ExclusiveGatewayInBatchTests();
		x.testSequentialTaskbased();
		x.testSequentialCasebased();
		//x.testParallel();
	}

	
	@Test
	public void testSequentialTaskbased() {
		executionType = BatchClusterExecutionType.SEQUENTIAL_TASKBASED;
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayExclusive.bpmn", 
				"BatchTestSimulationConfigurationWithXGateway.xml");
		File f = new File(".\\"+outputPath+"Process_1_processBatchActivityStats.csv");
		assertTrue(f.exists());
		List<String[]> table = TestUtils.readCSV(f);
		
		assertEquals(20, table.size());
		Map<String, List<String[]>> clusters = TestUtils.groupByCluster(table);
		
		for(List<String[]> cluster : clusters.values()) {
			assertExclusiveness(cluster);
		}
		
		for(List<String[]> processInstance : TestUtils.groupBy(table, 0).values()) {
			assertExclusiveness(processInstance);
		}
	}
	
	
	@Test
	public void testSequentialCasebased() {
		executionType = BatchClusterExecutionType.SEQUENTIAL_CASEBASED;
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayExclusive.bpmn", 
				"BatchTestSimulationConfigurationWithXGateway.xml");
		File f = new File(".\\"+outputPath+"Process_1_processBatchActivityStats.csv");
		assertTrue(f.exists());
		List<String[]> table = TestUtils.readCSV(f);
		
		assertEquals(20, table.size());
		Map<String, List<String[]>> clusters = TestUtils.groupByCluster(table);
		
		for(List<String[]> cluster : clusters.values()) {
			SequentialCasebasedTests.assertClusterIsCaseBased(cluster);
			assertExclusiveness(cluster);
		}
		
		for(List<String[]> processInstance : TestUtils.groupBy(table, 0).values()) {
			assertExclusiveness(processInstance);
		}
	}
	
	@Test
	public void testParallel() {
		executionType = BatchClusterExecutionType.PARALLEL;
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayExclusive.bpmn", 
				"BatchTestSimulationConfigurationWithXGateway.xml");
		File f = new File(".\\"+outputPath+"Process_1_processBatchActivityStats.csv");
		assertTrue(f.exists());
		List<String[]> table = TestUtils.readCSV(f);
		
		assertEquals(20, table.size());
		Map<String, List<String[]>> clusters = TestUtils.groupByCluster(table);

		for(List<String[]> cluster : clusters.values()) {
			ParallelTests.assertActivityGroupsAreEqual(cluster);
		}
		
		for(List<String[]> processInstance : TestUtils.groupBy(table, 0).values()) {
			assertExclusiveness(processInstance);
		}
	}
	
	private static void assertExclusiveness(List<String[]> processInstance) {
		assertTrue(
				processInstance.stream().anyMatch((any)->{return any[1].equals("Activity A");})
				^
				processInstance.stream().anyMatch((any)->{return any[1].equals("Activity B");})
		);
	}
	
	@Override
	protected void afterParsing() {
		super.afterParsing();
		changeBatchType();
	}
	
	protected void changeBatchType() {
		BatchActivity batchActivity = simulationManager.getProcessModels().get("Process_1").getBatchActivities().get(3);
		assert batchActivity != null;
		assert executionType != null;
		try {
			Field privateStringField = BatchActivity.class.getDeclaredField("executionType");
			privateStringField.setAccessible(true);
			privateStringField.set(batchActivity, executionType);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		assert batchActivity.getExecutionType() == executionType;
	}
		

}
