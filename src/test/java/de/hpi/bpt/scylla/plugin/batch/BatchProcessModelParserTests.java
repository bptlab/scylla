package de.hpi.bpt.scylla.plugin.batch;

import static de.hpi.bpt.scylla.TestUtils.assertAttribute;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;

public class BatchProcessModelParserTests extends BatchSimulationTest {
	
	public static void main(String[] args) {
		try {
			new BatchProcessModelParserTests().testBatchTaskIsParsed();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBatchSubprocessIsParsed() throws ScyllaValidationException, JDOMException, IOException {
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		simulationManager._parseInput();
		assertNotNull(getBatchActivity());
	}
	
	@Test
	public void testBatchTaskIsParsed() throws ScyllaValidationException, JDOMException, IOException {
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelBatchTask.bpmn", 
				"BatchTestSimulationConfigurationBatchTask.xml");
		simulationManager._parseInput();
		assertNotNull(getBatchActivity());
	}
	
	@Test
	public void testMaxBatchSizeParsing() throws ScyllaValidationException, JDOMException, IOException {
		Integer maxSize = new Random().nextInt(Integer.MAX_VALUE);
		before(() -> setProperty("maxBatchSize", maxSize));
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		simulationManager._parseInput();
		assertTrue(getBatchActivity().getMaxBatchSize().equals(maxSize));
	}
	
	@Test
	public void testNoMaxBatchSizeThrowsError() {
		before(() -> getProperty("maxBatchSize").detach());
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		assertThrows(ScyllaValidationException.class, () ->	simulationManager._parseInput());
	}
	
	@ParameterizedTest
	@EnumSource(BatchClusterExecutionType.class)
	public void testExecutionTypeParsing(BatchClusterExecutionType executionType) throws ScyllaValidationException, JDOMException, IOException {
		before(() -> setProperty("executionType", executionType.elementName));
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		simulationManager._parseInput();
		assertTrue(getBatchActivity().getExecutionType().equals(executionType));
	}
	
	@Test
	public void testWrongExecutionTypeParsingThrowsError() {
		before(() -> setProperty("executionType", "notAValidExecutionType"));
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		assertThrows(ScyllaValidationException.class, () ->	simulationManager._parseInput());
	}
	
	@ParameterizedTest
	@CsvSource({"4, PT40M"})
	public void testParseThresholdRuleWithTimeOut(String threshold, String timeout) throws ScyllaValidationException, JDOMException, IOException {
		before(() -> {
			Element activationRule = prepareActivationRule();
			activationRule.setAttribute("name", "thresholdRule");
			activationRule.setAttribute("threshold", threshold);
			activationRule.setAttribute("timeout", timeout.toString());
		});
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		simulationManager._parseInput();
		assertAttribute(getBatchActivity().getActivationRule(), "threshold", Integer.parseInt(threshold));
		assertAttribute(getBatchActivity().getActivationRule(), "timeOut", Duration.parse(timeout));	
	}
	
	@ParameterizedTest
	@CsvSource({"4, DataObjectX.DateY"})
	public void testParseThresholdRuleWithDueDate(String threshold, String dueDate) throws ScyllaValidationException, JDOMException, IOException {
		before(() -> {
			Element activationRule = prepareActivationRule();
			activationRule.setAttribute("name", "thresholdRule");
			activationRule.setAttribute("threshold", threshold);
			activationRule.setAttribute("duedate", dueDate);
		});
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		simulationManager._parseInput();
		assertAttribute(getBatchActivity().getActivationRule(), "threshold", Integer.parseInt(threshold));
		assertAttribute(getBatchActivity().getActivationRule(), "dueDate", dueDate);	
	}
	
	@ParameterizedTest
	@CsvSource({"4, DataObjectX.DateY"})
	public void testParseThresholdRuleWithoutThrowsError() throws ScyllaValidationException, JDOMException, IOException {
		before(() -> {
			Element activationRule = prepareActivationRule();
			activationRule.setAttribute("name", "thresholdRule");
			activationRule.setAttribute("threshold", "4");
		});
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		assertThrows(ScyllaValidationException.class, () ->	simulationManager._parseInput());
	}
	
	@ParameterizedTest
	@CsvSource({"1, PT10M, 2, PT30M"})
	public void testParseMinMaxRule(Integer minInstances, String minTimeout, Integer maxInstances, String maxTimeout) throws ScyllaValidationException, JDOMException, IOException {
		before(() -> {
			Element activationRule = prepareActivationRule();
			activationRule.setAttribute("name", "minMaxRule");
			activationRule.setAttribute("minInstances", minInstances.toString());
			activationRule.setAttribute("minTimeout", minTimeout);
			activationRule.setAttribute("maxInstances", maxInstances.toString());
			activationRule.setAttribute("maxTimeout", maxTimeout);
		});
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		simulationManager._parseInput();
		assertAttribute(getBatchActivity().getActivationRule(), "minInstances", minInstances);
		assertAttribute(getBatchActivity().getActivationRule(), "minTimeout", Duration.parse(minTimeout));
		assertAttribute(getBatchActivity().getActivationRule(), "maxInstances", maxInstances);
		assertAttribute(getBatchActivity().getActivationRule(), "maxTimeout", Duration.parse(maxTimeout));
	}
	
	@Test
	public void testWrongActivationRuleParsingThrowsError() {
		before(() -> getProperty("activationRule").getChildren().iterator().next().setAttribute("name", "notAValidActivationRule"));
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		assertThrows(ScyllaValidationException.class, () ->	simulationManager._parseInput());
	}
	
	@Test
	public void testTooManyActivationRulesParsingThrowsError() {
		before(() -> getProperty("activationRule").addContent(new Element("property",properties().getNamespace())));
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		assertThrows(ScyllaValidationException.class, () ->	simulationManager._parseInput());
	}
	
	@Test
	public void testDefaultActivationRule() throws ScyllaValidationException, JDOMException, IOException {
		before(() -> getProperty("activationRule").getContent().clear());
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		simulationManager._parseInput();
		assertNotNull(getBatchActivity().getActivationRule());
	}
	
	@Test
	public void testParseGroupingCharacteristic() throws ScyllaValidationException, JDOMException, IOException {
		String characteristic = "DataObjectX.ValueY";
		before(() -> prepareGroupingCharacteristic().setAttribute("name", "processVariable").setAttribute("value", "DataObjectX.ValueY"));
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		simulationManager._parseInput();
		assertTrue(getBatchActivity().getGroupingCharacteristic().contains(characteristic));
	}
	
	@Test
	public void testParseNoBatchRegion() throws ScyllaValidationException, JDOMException, IOException {
		before(() -> properties().getChildren().clear());
		createSimpleSimulationManager(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfiguration.xml");
		simulationManager._parseInput();
		assertTrue(BatchPluginUtils.getBatchActivities(getProcessModel()).isEmpty());
	}
	
	
	
	
	
	
	
	
	
	
	
	private Element processRoot() {
		return processRoots.get("Process_1");
	}
	
	private Element properties() {
		Namespace nsp = processRoot().getNamespace();
		return processRoot()
			.getChild("subProcess", nsp)
			.getChild("extensionElements", nsp)
			.getChildren().iterator().next();
	}
	
	private Element getProperty(String name) {
		return properties().getChildren().stream().filter(each -> each.getAttributeValue("name").equals(name)).findAny().get();
	}
	
	private void setProperty(String name, Object value) {
		getProperty(name).setAttribute("value", value.toString());
	}
	
	private Element prepareActivationRule() {
		Element activationRuleRoot = getProperty("activationRule");
		Element ruleElement = activationRuleRoot.getChildren().iterator().next();
		ruleElement.getAttributes().clear();
		return ruleElement;
	}
	
	private Element prepareGroupingCharacteristic() {
		Element groupingCharacteristic = getProperty("groupingCharacteristic");
		Element child = new Element("property", groupingCharacteristic.getNamespace());
		groupingCharacteristic.addContent(child);
		return child;
	}
	
	private void before(Runnable r) {
		beforeParsingModels.computeIfAbsent("Process_1",s -> new ArrayList<>()).add(r);
	}

}
