package de.hpi.bpt.scylla.GUI;

import static de.hpi.bpt.scylla.Scylla.FILEDELIM;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.hpi.bpt.scylla.TestUtils;
import de.hpi.bpt.scylla.GUI.SimulationConfigurationPane.SimulationConfigurationPane;

public class RegressionTests {
	
	/**
	 * When there was a parallel gateway after an exclusive gateway
	 * A NullPointerException in ExclusiveGatewayPanel.initBranches(ExclusiveGatewayPanel.java:66) occurred
	 * because the flow target was null.
	 * This was caused by parallel gateways not being registered as elements
	 */
	@Test
	public void testNoFlowTargetForParallelGateway() {
		Assertions.assertDoesNotThrow(() -> {
			SimulationConfigurationPane simConfPane =  simulationConfigurationPaneWithEmpty();
			setSimulationConfigurationPaneModel(simConfPane, "testNoFlowTargetForParallelGateway.bpmn");
			Method updateModel = SimulationConfigurationPane.class.getDeclaredMethod("updateModel");
			updateModel.setAccessible(true);
			try {
				updateModel.invoke(simConfPane);
			} catch (Exception e) {
				throw e.getCause();
			}
		}, "Error was thrown during gui building of model with X-gateway followed by +-gateway");
	}
	
	
	public String getFolderName() {
		return "GUI";
	}
	
	public SimulationConfigurationPane simulationConfigurationPaneWithEmpty() throws Throwable {
		SimulationConfigurationPane simConfPane =  new SimulationConfigurationPane();
		Method create = SimulationConfigurationPane.class.getDeclaredMethod("create");
		create.setAccessible(true);
		try {
			create.invoke(simConfPane);
		} catch (Exception e) {
			throw e.getCause();
		}
		return simConfPane;
	}
	
	public void setSimulationConfigurationPaneModel(SimulationConfigurationPane simConfPane, String model) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		String modelPath = TestUtils.RESOURCEFOLDER+getFolderName()+FILEDELIM+model;
		Field modelPathField = SimulationConfigurationPane.class.getDeclaredField("bpmnPath");
		modelPathField.setAccessible(true);
		modelPathField.set(simConfPane, modelPath);
	}

}
