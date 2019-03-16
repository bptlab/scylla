package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.hpi.bpt.scylla.TestUtils;
import de.hpi.bpt.scylla.plugin.batch.BatchCSVLogger.BatchCSVEntry;

public class MiscBatchTests extends BatchSimulationTest {
	
	@Test
	public void testStartMaxloadedClusters() {
		int maxBatchSize = 5;
		afterParsing(() -> {
			TestUtils.setAttribute(getBatchActivity().getActivationRule(), "threshold", 100);
			TestUtils.setAttribute(getBatchActivity().getActivationRule(), "timeOut", Duration.ofDays(10));
		});
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		for(List<BatchCSVEntry> cluster : getClusters().values()) {
			Set<Integer> processInstances = cluster.stream().map(BatchCSVEntry::getInstanceId).collect(Collectors.toSet());
			assertEquals(maxBatchSize, processInstances.size());
		}
	}

}
