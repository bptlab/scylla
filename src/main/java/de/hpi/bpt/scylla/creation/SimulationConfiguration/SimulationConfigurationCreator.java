package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.time.ZonedDateTime;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.creation.ElementLink;


public class SimulationConfigurationCreator extends ElementLink{

	/**jdom2 XML document object of the global configuration*/
	private Document doc;
	/**Root object of document*/
	private Element root;
	/**Scylla namespace*/
	private Namespace nsp;

	/**
	 * Public constructor,
	 * generates new empty SimulationConfiguration
	 */
	public SimulationConfigurationCreator(){
		super(new Element("simulationConfiguration",stdNsp));
		Element superroot = new Element("definitions",nsp);
		superroot.setAttribute("targetNamespace", "http://www.hpi.de");
		doc = new Document(superroot);
		root = el;
		superroot.addContent(root);
	}
	
	/**
	 * Getter for {@link Document de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator.doc}
	 * @return jdom2 Document
	 */
	public Document getDoc() {
		return doc;
	}
	
	/**
	 * Sets an attribute with given name to a value object, that is converted by its toString() method
	 * @param name attribute name
	 * @param value attribute value
	 */
	private void setAttribute(String name, Object value){
		root.setAttribute(name, value.toString());
	}

	public void setId(String id){setAttribute("id", id);}
	public String getId(){return root.getAttributeValue("id");}
	public void setProcessRef(String processRef){setAttribute("processRef", processRef);}
	public String getProcessRef(){return root.getAttributeValue("processRef");}
	public void setProcessInstances(int instances){setAttribute("processInstances", instances);}
	public String getProcessInstances(){return root.getAttributeValue("processInstances");}
	public void setStartDateTime(ZonedDateTime startTime){setAttribute("startDateTime", startTime);}
	public String getStartDateTime(){return root.getAttributeValue("startDateTime");}
	public void setEndDateTime(ZonedDateTime endTime){setAttribute("endDateTime", endTime);}
	public String getEndDateTime(){return root.getAttributeValue("endDateTime");}
	//TODO public void setResourceAssignmentOrder(String id){setAttribute("resourceAssignmentOrder", id);}
	public void setRandomSeed(int seed){setAttribute("randomSeed", seed);}
	public String getRandomSeed(){return root.getAttributeValue("randomSeed");}
	
	
	public void setModel(Element modelRoot){
		List<Element> processes = modelRoot.getChildren("process",modelRoot.getNamespace());
		Element process = null;
		for(Element p : processes){
			if(p.getAttributeValue("id").equals(getProcessRef())){
				process = p;
				break;
			}
		}
		if(process == null){
			System.out.println("Not found");
			return;
		}
		for(Element child : process.getChildren()){
			if(isKnownModelElement(child.getName()))
				createModelElement(child);
				System.out.println(child.getQualifiedName());
		}
	}
	
	
    private void createModelElement(Element child) {
		String name = child.getName();
		switch(name){
		//TODO
		}
	}

	private boolean isKnownModelElement(String name) {
        return name.equals("sequenceFlow") || name.equals("task") || name.endsWith("Task") || name.endsWith("Event")
                || name.endsWith("Gateway") || name.equals("subProcess") || name.equals("callActivity")
                || name.equals("laneSet") || name.equals("dataObjectReference") || name.equals("ioSpecification")
                || name.equals("dataObject");
    }
	
	
}
