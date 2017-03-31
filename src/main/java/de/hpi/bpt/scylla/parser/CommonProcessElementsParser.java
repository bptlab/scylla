package de.hpi.bpt.scylla.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.CommonProcessElements;
import de.hpi.bpt.scylla.model.process.node.GlobalTaskType;

/**
 * Parses BPMN process elements which are defined in the common process elements part, i.e. they are valid for all
 * processes defined in a BPMN file.
 * 
 * @author Tsun Yin Wong
 *
 */
public class CommonProcessElementsParser extends Parser<CommonProcessElements> {

    public CommonProcessElementsParser(SimulationManager simEnvironment) {
        super(simEnvironment);
    }

    @Override
    public CommonProcessElements parse(Element rootElement) throws ScyllaValidationException {
        String definitionsId = rootElement.getAttributeValue("id");

        Namespace bpmnNamespace = rootElement.getNamespace();

        Map<String, GlobalTaskType> globalTasks = new HashMap<String, GlobalTaskType>();
        Map<String, Element> globalTaskElements = new HashMap<String, Element>();
        Map<String, Map<String, String>> resources = new HashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> messages = new HashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> errors = new HashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> escalations = new HashMap<String, Map<String, String>>();

        // global tasks called by call activities
        for (GlobalTaskType gtt : GlobalTaskType.values()) {
            List<Element> gte = rootElement.getChildren(gtt.toString(), bpmnNamespace);
            for (Element el : gte) {
                String elementId = el.getAttributeValue("id");
                globalTasks.put(elementId, gtt);
                globalTaskElements.put(elementId, el);
            }
        }

        // common elements: chapter 8.4 in BPMN 2.0.2 definition

        List<Element> resourceElements = rootElement.getChildren("resource", bpmnNamespace);
        for (Element el : resourceElements) {
            Map<String, String> resource = new HashMap<String, String>();
            String elementId = el.getAttributeValue("id");
            String name = el.getAttributeValue("name");
            if (name != null) {
                resource.put("name", name);
            }
            resources.put(elementId, resource);
        }

        List<Element> messageElements = rootElement.getChildren("message", bpmnNamespace);
        for (Element el : messageElements) {
            Map<String, String> message = new HashMap<String, String>();
            String elementId = el.getAttributeValue("id");
            String name = el.getAttributeValue("name");
            if (name != null) {
                message.put("name", name);
            }
            messages.put(elementId, message);
        }

        List<Element> errorElements = rootElement.getChildren("error", bpmnNamespace);
        for (Element el : errorElements) {
            Map<String, String> error = new HashMap<String, String>();
            String elementId = el.getAttributeValue("id");
            String name = el.getAttributeValue("name");
            if (name != null) {
                error.put("name", name);
            }
            String errorCode = el.getAttributeValue("errorCode");
            if (errorCode != null) {
                error.put("errorCode", errorCode);
            }
            errors.put(elementId, error);
        }

        List<Element> escalationElements = rootElement.getChildren("escalation", bpmnNamespace);
        for (Element el : escalationElements) {
            Map<String, String> escalation = new HashMap<String, String>();
            String elementId = el.getAttributeValue("id");
            String name = el.getAttributeValue("name");
            if (name != null) {
                escalation.put("name", name);
            }
            String escalationCode = el.getAttributeValue("escalationCode");
            if (escalationCode != null) {
                escalation.put("escalationCode", escalationCode);
            }
            escalations.put(elementId, escalation);
        }

        CommonProcessElements commonProcessElements = new CommonProcessElements(definitionsId, globalTasks,
                globalTaskElements, resources, messages, errors, escalations);
        return commonProcessElements;
    }

}
