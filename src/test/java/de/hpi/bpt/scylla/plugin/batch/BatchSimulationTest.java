package de.hpi.bpt.scylla.plugin.batch;

import java.lang.reflect.Field;

import de.hpi.bpt.scylla.SimulationTest;

public class BatchSimulationTest extends SimulationTest{
	

	protected BatchClusterExecutionType executionType;

	@Override
	protected String getFolderName() {return "BatchPlugin";}
	
	@Override
	protected void afterParsing() {
		super.afterParsing();
		changeBatchType();
	}
	
	protected void changeBatchType() {
		BatchActivity batchActivity = simulationManager.getProcessModels().values().iterator().next().getBatchActivities().values().iterator().next();
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
