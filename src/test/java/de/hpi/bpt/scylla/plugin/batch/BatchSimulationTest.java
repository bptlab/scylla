package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.SimulationTest;
import de.hpi.bpt.scylla.TestUtils;
import de.hpi.bpt.scylla.plugin.batch.BatchCSVLogger.BatchCSVEntry;

public class BatchSimulationTest extends SimulationTest{
	

	protected BatchClusterExecutionType executionType;
	protected List<BatchCSVEntry> table;

	@Override
	protected String getFolderName() {return "BatchPlugin";}
	
	@Override
	protected void afterParsing() {
		super.afterParsing();
		if(executionType != null)changeBatchType();
	}
	
	@Override
	protected void runSimpleSimulation(String globalConfiguration, String simulationModel, String simulationConfiguration) {
		super.runSimpleSimulation(globalConfiguration, simulationModel, simulationConfiguration);
		parseTable();
	}
	
	protected void changeBatchType() {
		BatchActivity batchActivity = getBatchActivity();
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
	
	protected void parseTable() {
		File f = new File(".\\"+outputPath+getProcessId()+"_processBatchActivityStats.csv");
		assertTrue(f.exists());
		table = TestUtils.readCSV(f).stream().map(BatchCSVEntry::fromArray).collect(Collectors.toList());
	}
	
	protected void assertExecutionType() {
		table.stream().forEach((each)->{if(!each.getBatchNumber().isEmpty())assertEquals(executionType, each.getBatchType());});
	}
	
	protected Map<String, List<BatchCSVEntry>> getClusters() {
		Map<String, List<BatchCSVEntry>> clusters = TestUtils.groupBy(table, BatchCSVEntry::getBatchNumber);
		clusters.remove("");
		return clusters;
	}
	
	protected Map<Integer, List<BatchCSVEntry>> getProcessInstances() {
		return TestUtils.groupBy(table, BatchCSVEntry::getInstanceId);
	}
	
	protected BatchActivity getBatchActivity() {
		return BatchPluginUtils.getBatchActivities(getProcessModel()).values().iterator().next();
	}

	protected void assertNoIntersections(List<BatchCSVEntry> rows) {
		rows = rows.stream()
				.sorted((a,b) -> {return a.getStart().compareTo(b.getStart());})
				.collect(Collectors.toList());
		for(int i = 0; i < rows.size()-1; i++) {
			BatchCSVEntry current = rows.get(i);
			BatchCSVEntry next = rows.get(i+1);
			assertTrue(current.getComplete().compareTo(next.getStart()) <= 0);
		}
	}
	

}
