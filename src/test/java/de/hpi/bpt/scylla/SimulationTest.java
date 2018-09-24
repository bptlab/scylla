package de.hpi.bpt.scylla;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Before;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;

public abstract class SimulationTest {


	protected abstract String getFolderName();
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
		createSimpleSimulationManager(globalConfiguration, simulationModel, simulationConfiguration);
		outputPath = simulationManager.run();
	}
	
	protected void createSimpleSimulationManager(String globalConfiguration, String simulationModel, String simulationConfiguration) {
		simulationManager = new SimulationManager(
				getFolder(), 
				new String[] {getPath(simulationModel)}, 
				new String[] {getPath(simulationConfiguration)}, 
				getPath(globalConfiguration),
                true, false) {
			@Override
			protected void parseInput() throws ScyllaValidationException, JDOMException, IOException {
				super.parseInput();
				SimulationTest.this.afterParsing();
			}
		};
	}
	
	@After
	public void tearDown() {
		TestUtils.deleteFolder(new File(".\\"+outputPath));
		TestUtils.cleanupOutputs(".\\"+getFolder());
	}
	
	protected int numberOfInstances() {
		return simulationManager.getSimulationConfigurations().values().iterator().next().getNumberOfProcessInstances();
	}
	
	protected void afterParsing() {}
	
	protected String getFolder() {
		return TestUtils.RESOURCEFOLDER+getFolderName()+"\\";
	}
	
	protected String getPath(String fileName) {
		String path = TestUtils.RESOURCEFOLDER;
		if(fileName.startsWith(".\\")) {
			path += fileName.substring(2);
		} else {
			path += getFolderName()+"\\"+fileName;
		}
		return path;
	}
	
}
