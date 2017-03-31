package de.hpi.bpt.scylla.model.process;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

import de.hpi.bpt.scylla.model.SimulationInput;
import de.hpi.bpt.scylla.model.process.node.GlobalTaskType;

/**
 * Represents all BPMN process elements which are defined in the common process elements part, i.e. they are valid for
 * all processes defined in a BPMN file.
 * 
 * @author Tsun Yin Wong
 * 
 */
public class CommonProcessElements extends SimulationInput {

    private Map<String, GlobalTaskType> globalTasks = new HashMap<String, GlobalTaskType>();
    private Map<String, Element> globalTaskElements = new HashMap<String, Element>();
    private Map<String, Map<String, String>> resources = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> messages = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> errors = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> escalations = new HashMap<String, Map<String, String>>();
    private String bpmnFileNameWithoutExtension;

    /**
     * Constructor.
     * 
     * @param id
     *            identifier of BPMN file
     * @param globalTasks
     *            map of global task identifier to its type
     * @param globalTaskElements
     *            map of global task identifier to its JDOM element
     * @param resources
     *            map of resource identifier to its attribute map (e.g. name=...)
     * @param messages
     *            map of message identifier to its attribute map (e.g. name=...)
     * @param errors
     *            map of error identifier to its attribute map (e.g. name=...)
     * @param escalations
     *            map of escalation identifier to its attribute map (e.g. name=...)
     */
    public CommonProcessElements(String id, Map<String, GlobalTaskType> globalTasks,
            Map<String, Element> globalTaskElements, Map<String, Map<String, String>> resources,
            Map<String, Map<String, String>> messages, Map<String, Map<String, String>> errors,
            Map<String, Map<String, String>> escalations) {
        super(id);
        this.globalTasks = globalTasks;
        this.globalTaskElements = globalTaskElements;
        this.resources = resources;
        this.messages = messages;
        this.errors = errors;
        this.escalations = escalations;
    }

    public Map<String, GlobalTaskType> getGlobalTasks() {
        return globalTasks;
    }

    public Map<String, Element> getGlobalTaskElements() {
        return globalTaskElements;
    }

    public Map<String, Map<String, String>> getResources() {
        return resources;
    }

    public Map<String, Map<String, String>> getMessages() {
        return messages;
    }

    public Map<String, Map<String, String>> getErrors() {
        return errors;
    }

    public Map<String, Map<String, String>> getEscalations() {
        return escalations;
    }

    public String getBpmnFileNameWithoutExtension() {
        return bpmnFileNameWithoutExtension;
    }

    public void setBpmnFileNameWithoutExtension(String bpmnFileNameWithoutExtension) {
        this.bpmnFileNameWithoutExtension = bpmnFileNameWithoutExtension;
    }

}
