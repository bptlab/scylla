package de.hpi.bpt.scylla;

import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.parser.CommonProcessElementsParser;
import de.hpi.bpt.scylla.parser.SimulationConfigurationParser;

public class SimulationManagerForTests extends SimulationManager{
	
	protected SimulationTest test;
	
	public SimulationManagerForTests(SimulationTest test, String globalConfiguration, String simulationModel, String simulationConfiguration) {
		super(test.getFolder(), 
			new String[] {test.getPath(simulationModel)}, 
			new String[] {test.getPath(simulationConfiguration)}, 
			test.getPath(globalConfiguration), 
			true, 
			false);
		this.test = test;
	}
	
	@Override
	protected void parseGlobalConfiguration(Element globalConfigRoot) throws ScyllaValidationException {
		test.beforeParsingGlobal(globalConfigRoot);
		super.parseGlobalConfiguration(globalConfigRoot);
	}
	
	@Override
	protected void parseProcessCommonsAndModel(CommonProcessElementsParser cpeParser, Element pmRootElement, String filename) throws ScyllaValidationException {
		test.beforeParsingModels(pmRootElement);
		super.parseProcessCommonsAndModel(cpeParser, pmRootElement, filename);
	}
	
	@Override
	protected void parseSimulationConfiguration(SimulationConfigurationParser simParser, Document scDoc) throws ScyllaValidationException {
		test.beforeParsingSimconfig(scDoc.getRootElement());
		super.parseSimulationConfiguration(simParser, scDoc);
	}
	
	@Override
	protected void parseInput() throws ScyllaValidationException, JDOMException, IOException {
		super.parseInput();
		test.afterParsing();
		test.afterParsing.ifPresent(Runnable::run);
	}
	
	public void _parseInput() throws ScyllaValidationException, JDOMException, IOException {
		parseInput();
	}

}
