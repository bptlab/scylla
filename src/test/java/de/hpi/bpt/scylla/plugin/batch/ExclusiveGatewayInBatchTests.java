package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.hpi.bpt.scylla.TestUtils;
import de.hpi.bpt.scylla.logger.DebugLogger;

public class ExclusiveGatewayInBatchTests extends BatchSimulationTest{
	
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
		

}
