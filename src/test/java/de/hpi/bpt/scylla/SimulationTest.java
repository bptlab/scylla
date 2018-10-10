package de.hpi.bpt.scylla;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;

import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Before;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.global.GlobalConfiguration;
import de.hpi.bpt.scylla.model.global.resource.Resource;
import de.hpi.bpt.scylla.model.process.ProcessModel;

public abstract class SimulationTest {


	protected abstract String getFolderName();
	protected SimulationManager simulationManager;
	protected String outputPath;

	protected PrintStream errorLog;
	protected PrintStream outputLog;
	
	protected Optional<Runnable> afterParsing = Optional.empty();
	
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
				afterParsing.ifPresent(Runnable::run);
			}
		};
	}
	
	@After
	public void tearDown() {
		TestUtils.deleteFolder(new File(".\\"+outputPath));
		TestUtils.cleanupOutputs(".\\"+getFolder());
	}
	
	protected GlobalConfiguration getGlobalConfiguration() {
		return simulationManager.getGlobalConfiguration();
	}
	
	protected SimulationConfiguration getSimulationConfiguration() {
		return simulationManager.getSimulationConfigurations().values().iterator().next();
	}
	
	protected ProcessModel getProcessModel() {
		return simulationManager.getProcessModels().values().iterator().next();
	}
	
	protected String getProcessId() {
		return getProcessModel().getId();
	}
	
	protected Resource getResource() {
		return getGlobalConfiguration().getResources().values().iterator().next();
	}
	
	protected int numberOfInstances() {
		return getSimulationConfiguration().getNumberOfProcessInstances();
	}
	
	/**
	 * Allows to add special after parsing behavior for all tests of one class, can be overriden
	 */
	protected void afterParsing() {}
	
	/**
	 * Allows to add special after parsing behavior for one test of a class (for all tests of one class use {@link #afterParsing()})
	 * @param r : A runnable to  be executed after parsing
	 */
	protected final void afterParsing(Runnable r) {
		afterParsing = Optional.of(r);
	}
	
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
