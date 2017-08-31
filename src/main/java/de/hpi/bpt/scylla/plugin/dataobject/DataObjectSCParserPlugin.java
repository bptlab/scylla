package de.hpi.bpt.scylla.plugin.dataobject;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.configuration.distribution.Distribution;
import de.hpi.bpt.scylla.model.configuration.distribution.UniformDistribution;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.parser.SimulationConfigurationParser;
import de.hpi.bpt.scylla.plugin_type.parser.SimulationConfigurationParserPluggable;

public class DataObjectSCParserPlugin extends SimulationConfigurationParserPluggable {

    @Override
    public String getName() {
        return DataObjectPluginUtils.PLUGIN_NAME;
    }

    @Override
    public Map<String, Object> parse(SimulationConfiguration simulationInput, Element sim)
            throws ScyllaValidationException {

        Namespace simNamespace = sim.getNamespace();

        ProcessModel processModel = simulationInput.getProcessModel();
        
        Map<Integer, Map<String, DataObjectField>> dataObjects = new HashMap<Integer, Map<String, DataObjectField>>();

        for (Element el : sim.getChildren()) {
            String elementName = el.getName();
            
            if (elementName.equals("dataObject") || 
            		elementName.equals("dataInput")) {

                String identifier = el.getAttributeValue("id");
                
                //Node<Integer> nodes =
                /*Collection<Node<Integer>> nodeArray = processModel.getDataObjectsGraph().getNodes().values();
                for (Node<Integer> node : nodeArray) {
                	System.out.println(node.getNodeId());
                }*/
                
                
                                
                if (identifier == null) {
                    DebugLogger.log("Warning: Simulation configuration definition element '" + elementName
                            + "' does not have an identifier, skip.");
                    continue; // no matching element in process, so skip definition
                }
                
                Integer nodeId = processModel.getIdentifiersToNodeIds().get(identifier);
                if (nodeId == null) {
                    DebugLogger.log("Simulation configuration definition for process element '" + identifier
                            + "', but not available in process, skip.");
                    continue; // no matching element in process, so skip definition
                }

            	Map<String, DataObjectField> dataObjectFields = new HashMap<String, DataObjectField>();
            	
            	for(Element field : el.getChildren("field", simNamespace)) {
            		String fieldName = field.getAttributeValue("name");
            		
                	String fieldType = field.getAttributeValue("type");
                	DataDistributionType dataDistributionType = DataDistributionType.getEnum(fieldType);
                	DataDistributionWrapper distWrapper = new DataDistributionWrapper(dataDistributionType);
                	
                	for(Element fieldElement : field.getChildren()){
                		
                		if(fieldElement.getName().endsWith("Distribution")) {
                			Distribution distribution = SimulationConfigurationParser.getDistribution(field, simNamespace);
                			distWrapper.setDistribution(distribution);
                		} else if (fieldElement.getName().equals("range")) {
                			try{
                				double min = Double.parseDouble(fieldElement.getAttributeValue("min"));
                				distWrapper.setMin(min);
                			} catch (NumberFormatException e) {
                				// do nothing: min was not set and is automatically -Double.MAX_VALUE
                			}
                			
                			try{
                				double max = Double.parseDouble(fieldElement.getAttributeValue("max"));
                				distWrapper.setMax(max);
                			} catch (NumberFormatException e) {
                				// do nothing: max was not set and is automatically Double.MAX_VALUE
                			}
                			Distribution distribution = new UniformDistribution(distWrapper.getMin(), distWrapper.getMax());
                			distWrapper.setDistribution(distribution);
                		}
                	}
                	dataObjectFields.put(fieldName, new DataObjectField(distWrapper, nodeId));
            	}
            	dataObjects.put(nodeId, dataObjectFields);
            }
        }
        System.out.println(processModel.getDataObjectsGraph().print());
        HashMap<String, Object> extensionAttributes = new HashMap<String, Object>();
        extensionAttributes.put("dataObjects", dataObjects);
        
        return extensionAttributes;
    }
}