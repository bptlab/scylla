package de.hpi.bpt.scylla;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static de.hpi.bpt.scylla.Scylla.*;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.global.GlobalConfiguration;
import de.hpi.bpt.scylla.model.global.resource.Resource;
import de.hpi.bpt.scylla.model.process.ProcessModel;

public abstract class SimulationTest {


	protected abstract String getFolderName();
	protected SimulationManagerForTests simulationManager;
	protected String outputPath;

	protected PrintStream errorLog;
	protected PrintStream outputLog;

	protected List<Runnable> beforeParsingGlobal = new LinkedList<>();
	protected Element globalConfigRoot;
	protected Map<String, List<Runnable>> beforeParsingSims = new HashMap<>();
	protected Map<String, Element> simConfigRoots = new HashMap<>();
	protected Map<String, List<Runnable>> beforeParsingModels = new HashMap<>();
	protected Map<String, Element> processRoots = new HashMap<>();

	protected Optional<Runnable> afterParsing = Optional.empty();
	
	@BeforeEach
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
		simulationManager = new SimulationManagerForTests(this, globalConfiguration, simulationModel, simulationConfiguration);
	}

	@AfterEach
	public void tearDown() {
		TestUtils.deleteFolder(new File("."+FILEDELIM+outputPath));
		TestUtils.cleanupOutputs("."+FILEDELIM+getFolder());
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
	
	protected void setGlobalSeed(Long seed) {
		if(seed == null)return;
		if(globalConfigRoot == null)beforeParsingGlobal.add(()->setGlobalSeed(seed));
		else {		
			Namespace nsp = globalConfigRoot.getNamespace();
			if(globalConfigRoot.getChild("randomSeed",nsp) == null)globalConfigRoot.addContent(new Element("randomSeed",nsp));
			globalConfigRoot.getChild("randomSeed",nsp).setText(seed.toString());
		}
	}
	
	protected void beforeParsingGlobal(Element globalConfigRoot) {
		this.globalConfigRoot = globalConfigRoot;
		beforeParsingGlobal.stream().forEach(Runnable::run);
	}
	
	
	protected void beforeParsingSimconfig(Element simConfigRoot) {
        Namespace simNamespace = simConfigRoot.getNamespace();
        List<Element> simElements = simConfigRoot.getChildren("simulationConfiguration", simNamespace);
        for(Element simElement : simElements) {
        	String id = simElement.getAttributeValue("id");
        	simConfigRoots.put(id, simElement);
        	beforeParsingSims.getOrDefault(id, Collections.emptyList()).forEach(Runnable::run);
        }
	}
	
	protected void beforeParsingModels(Element modelRoot) {
        Namespace bpmnNamespace = modelRoot.getNamespace();
        List<Element> processElements = modelRoot.getChildren("process", bpmnNamespace);
        for(Element process : processElements) {
        	String id = process.getAttributeValue("id");
        	processRoots.put(id, process);
        	beforeParsingModels.getOrDefault(id, Collections.emptyList()).forEach(Runnable::run);
        }
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
		return TestUtils.RESOURCEFOLDER+getFolderName()+FILEDELIM;
	}
	
	protected String getPath(String fileName) {
		String path = TestUtils.RESOURCEFOLDER;
		path = normalizePath(path);
		if(fileName.startsWith("."+FILEDELIM)) {
			path += fileName.substring(2);
		} else {
			path += getFolderName()+FILEDELIM+fileName;
		}
		return path;
	}
	
}
