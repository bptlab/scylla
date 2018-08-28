package de.hpi.bpt.scylla;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;

import de.hpi.bpt.scylla.SimulationManager;

public abstract class SimulationTest {


	protected abstract String getFolder();
	protected SimulationManager simulationManager;
	protected String outputPath;

	protected PrintStream errorLog;
	protected PrintStream outputLog;
	
	@Before
	public void setUp() {
		errorLog = new PrintStream(new ByteArrayOutputStream());
		outputLog = new PrintStream(new ByteArrayOutputStream());
		System.setErr(errorLog);
		System.setOut(outputLog);
	}
	
	protected void runSimpleSimulation(String globalConfiguration, String simulationModel, String simulationConfiguration) {
		simulationManager = new SimulationManager(
				getFolder(), 
				new String[] {getFolder()+simulationModel}, 
				new String[] {getFolder()+simulationConfiguration}, 
				getFolder()+globalConfiguration,
                true, false);
		outputPath = simulationManager.run();
	}
	
	@After
	public void tearDown() {
		TestUtils.deleteFolder(new File(".\\"+outputPath));
		TestUtils.cleanupOutputs(".\\"+getFolder());
	}
	
	protected int numberOfInstances() {
		return simulationManager.getSimulationConfigurations().values().iterator().next().getNumberOfProcessInstances();
	}
	
}
