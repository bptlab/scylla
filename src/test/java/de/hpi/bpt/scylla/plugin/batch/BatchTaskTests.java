package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class BatchTaskTests extends BatchSimulationTest {
	
	public static void main(String[] args) {
		try {
			new BatchTaskTests().testAllInstancesAreFinished(BatchClusterExecutionType.PARALLEL);
			//new CSVLoggerTests().testLogIsCreated("ModelSimple.bpmn", "BatchTestSimulationConfiguration.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@ParameterizedTest
	@EnumSource(BatchClusterExecutionType.class)
	public void testAllInstancesAreFinished(BatchClusterExecutionType executionType) {
		this.executionType = executionType;
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelBatchTask.bpmn", 
				"BatchTestSimulationConfigurationBatchTask.xml");
		
		assertEquals(30, table.size());
		table.stream().forEach(each -> {
			try {
				assertNotNull(BatchCSVLogger.timeFormat.parse(each[2]));
				assertNotNull(BatchCSVLogger.timeFormat.parse(each[3]));
				assertNotNull(BatchCSVLogger.timeFormat.parse(each[4]));
			} catch (ParseException e) {
				e.printStackTrace();
				fail(e);
			}
		});
		
	}

}
