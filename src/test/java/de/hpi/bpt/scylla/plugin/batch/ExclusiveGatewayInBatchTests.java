package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.hpi.bpt.scylla.logger.DebugLogger;

public class ExclusiveGatewayInBatchTests extends BatchSimulationTest{
	
	public static void main(String[] args) {
		DebugLogger.allowDebugLogging = false;
		ExclusiveGatewayInBatchTests x = new ExclusiveGatewayInBatchTests();
		x.testSequentialTaskbased();
		x.testSequentialCasebased();
		x.testParallel();
	}

	
	@Test
	public void testSequentialTaskbased() {
		executionType = BatchClusterExecutionType.SEQUENTIAL_TASKBASED;
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelGatewayExclusive.bpmn", 
				"BatchTestSimulationConfigurationWithXGateway.xml");
		
		assertEquals(20, table.size());
		
		for(List<String[]> cluster : getClusters().values()) {
			assertExclusiveness(cluster);
		}
		
		for(List<String[]> processInstance : getProcessInstances().values()) {
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
		
		for(List<String[]> cluster : getClusters().values()) {
			SequentialCasebasedTests.assertClusterIsCaseBased(cluster);
			assertExclusiveness(cluster);
		}
		
		for(List<String[]> processInstance : getProcessInstances().values()) {
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
		
		for(List<String[]> cluster : getClusters().values()) {
			ParallelTests.assertActivityGroupsAreEqual(cluster);
		}
		
		for(List<String[]> processInstance : getProcessInstances().values()) {
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
