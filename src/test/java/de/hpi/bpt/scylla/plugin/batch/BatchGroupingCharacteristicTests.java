package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.hpi.bpt.scylla.plugin.batch.BatchCSVLogger.BatchCSVEntry;
import de.hpi.bpt.scylla.plugin.dataobject.DataObjectField;

public class BatchGroupingCharacteristicTests extends BatchSimulationTest {
	
	public static void main(String[] args) {
		new BatchGroupingCharacteristicTests().testSingleValuePerBatch();
	}
	
	@Test
	public void testSingleValuePerBatch() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelWithGrouping.bpmn", 
				"BatchTestSimulationConfigurationWithDataObject.xml");
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			Stream<Integer> processes = cluster.stream().map(BatchCSVEntry::getInstanceId);
			assertEquals(
				1, 
				processes
					.map(each -> DataObjectField.getDataObjectValue(each,"DataObject.Value"))
					.collect(Collectors.toSet())
					.size());
		}
	}
	
	
	@Test
	public void testInstancesAreGrouped() {
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelWithGrouping.bpmn", 
				"BatchTestSimulationConfigurationWithDataObject.xml");
		assertTrue(getClusters().values().stream().anyMatch(any -> any.size() > 1));
	}
	
	

}
