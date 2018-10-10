package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import de.hpi.bpt.scylla.SimulationTest;

public class BatchUtilsTests extends SimulationTest{
	

	@Override
	protected String getFolderName() {return "BatchPlugin";}
	
	@Test
	public void testFreshInstance() {
		assertFalse(BatchPluginUtils.isInitialized());
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		assertFalse(BatchPluginUtils.isInitialized());
	}


}
