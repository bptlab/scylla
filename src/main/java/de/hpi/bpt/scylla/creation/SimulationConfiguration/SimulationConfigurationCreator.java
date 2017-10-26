package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.hpi.bpt.scylla.creation.ElementLink;


public class SimulationConfigurationCreator extends ElementLink{

	/**jdom2 XML document object of the global configuration*/
	private Document doc;
	/**Root object of document*/
	private Element root;
	
	/**Start event element*/
	private StartEvent startEvent;
	/**Task list*/
	private Map<String,ElementLink> elements;
	
	private Map<String,String[]> flows;
	
	/**
	 * Public constructor,
	 * generates new empty SimulationConfiguration, linked with simulationConfiguration element
	 */
	public SimulationConfigurationCreator(){
		super(new Element("simulationConfiguration", stdNsp));
		Element superroot = new Element("definitions",nsp);
		superroot.setAttribute("targetNamespace", "http://www.hpi.de");
		doc = new Document(superroot);
		root = el;
		superroot.addContent(root);
		elements = new TreeMap<String,ElementLink>();
		flows = new HashMap<String,String[]>();
		startEvent = new StartEvent("startEvent");
	}
	
	private SimulationConfigurationCreator(Element r, Document d) {
		super(r.getChild("simulationConfiguration", stdNsp));
		root = el;
		doc = d;
		
		elements = new TreeMap<String,ElementLink>();
		flows = new HashMap<String,String[]>();
		
		addProcessSCElements(root);
	}

	/**
	 * Getter for {@link Document de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator.doc}
	 * @return jdom2 Document
	 */
	public Document getDoc() {
		return doc;
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
	public void removeEndDateTime(){root.removeAttribute("endDateTime");}
	//TODO public void setResourceAssignmentOrder(String id){setAttribute("resourceAssignmentOrder", id);}
	public void setRandomSeed(long seed){
		if(root.getChild("randomSeed",nsp) == null)root.addContent(new Element("randomSeed",nsp));
		root.getChild("randomSeed",nsp).setText(seed+"");
	}
	public void removeRandomSeed(){root.removeAttribute("randomSeed");}
	public Integer getRandomSeed(){
		if(root.getChild("randomSeed",nsp) == null)return null;
		return Integer.parseInt(root.getChild("randomSeed",nsp).getText());
	}
	
	public StartEvent getStartEvent(){return startEvent;}
	
	
	
	public void setModel(Element modelRoot){
		List<Element> processes = modelRoot.getChildren("process",modelRoot.getNamespace());
		if(processes.size() == 0){
			System.err.println("No process was found in the business model file");
			return;
		}
		Element process = null;
		String ref = getProcessRef();
		if(ref == null || ref.isEmpty()){
				process = processes.get(0);
				setProcessRef(process.getAttributeValue("id"));
		}else{
			for(Element p : processes){
				if(p.getAttributeValue("id").equals(ref)){
					process = p;
					break;
				}
			}
		}

		if(process == null){
			System.err.println("Process matching to id"+ref+"not found");
			return;
		}
		
		addProcessModelElements(process,this);

	}
	
	public void addProcessModelElements(Element process, ElementLink addTo){
		for(Element child : process.getChildren()){
			if(isKnownModelElement(child.getName()))
				createModelElement(child, addTo);
				//System.out.println(child.getQualifiedName());
		}
	}
	
	
    private void createModelElement(Element child, ElementLink addTo) {
		String name = child.getName();
		switch(name){
		case "startEvent" :
			if(!addTo.equals(this))break;//ignore subProcess startEvents
			startEvent = new StartEvent(child.getAttributeValue("id"));
			startEvent.addTo(addTo);
			break;
			
		case "subProcess" :
			SubProcess s = new SubProcess(child.getAttributeValue("id"),child.getAttributeValue("name"));
			s.addTo(addTo);
			elements.put(s.getId(),s);
			addProcessModelElements(child,s);
			break;
			
		case "exclusiveGateway" :
			List<Element> branches = child.getChildren();//Note: getChildren("outgoing",Nsp); not possible as there is no single unique namespace
			List<String> branchids = new ArrayList<String>();
			for(Element b : branches){
				if(b.getName().equals("outgoing"))branchids.add(b.getValue());
			}
			ExclusiveGateway eg = new ExclusiveGateway(child.getAttributeValue("id"),branchids);
			eg.addTo(addTo);
			elements.put(eg.getId(),eg);
			break;
			
		case "sequenceFlow" :
			flows.put(child.getAttributeValue("id"), new String[]{child.getAttributeValue("sourceRef"),child.getAttributeValue("targetRef")});
			break;
			
		default :
			if(name.equals("task") || name.endsWith("Task")){
				Task t = new Task(child.getAttributeValue("id"),child.getAttributeValue("name"));
				t.addTo(addTo);
				elements.put(t.getId(),t);
			}break;
		}
	}
    
    private void addProcessSCElements(Element root){
		for(Element e : root.getChildren()){
			String name = e.getName();
			if(isKnownSCElement(name)){
				/*        return name.equals("task") || name.endsWith("Task") || name.equals("startEvent") || name.equals("subProcess")
                || name.equals("resources");*/
				if(name.equals("startEvent")){
					startEvent = new StartEvent(e);
				}else if(name.equals("task") || name.endsWith("Task")){
					Task t = new Task(e);
					elements.put(t.getId(), t);
				}else if(name.equals("subProcess")){
					SubProcess p = new SubProcess(e);
					elements.put(p.getId(), p);
					addProcessSCElements(e);
				}else if(name.equals("exclusiveGateway")){
					ExclusiveGateway g = new ExclusiveGateway(e);
					elements.put(g.getId(),g);
				}
			}
		}
    }

    /**{@link de.hpi.bpt.scylla.parser.ProcessModelParser#isKnownElement(String name)}*/
	private boolean isKnownModelElement(String name) {
	      return name.equals("task") || name.endsWith("Task")
	      || name.endsWith("Gateway") || name.equals("subProcess")
	      || name.equals("sequenceFlow") || name.equals("startEvent");
//        return name.equals("sequenceFlow") || name.equals("task") || name.endsWith("Task") || name.endsWith("Event")
//                || name.endsWith("Gateway") || name.equals("subProcess") || name.equals("callActivity")
//                || name.equals("laneSet") || name.equals("dataObjectReference") || name.equals("ioSpecification")
//                || name.equals("dataObject");
    }
	
	/**@see {@link de.hpi.bpt.scylla.parser.SimulationConfigurationParser#isKnownElement(String name)}*/
	private boolean isKnownSCElement(String name){
        return name.equals("task") || name.endsWith("Task") || name.equals("startEvent") || name.equals("subProcess")
                || name.equals("resources") || name.equals("exclusiveGateway");
	}
	
	
	
	public ElementLink getElement(String id){
		return elements.get(id);
	}
	
	public Collection<ElementLink> getElements(){
		return elements.values();
	}

	public ElementLink getFlowSource(String id){
		return elements.get(flows.get(id)[0]);
	}

	public ElementLink getFlowTarget(String id){
		return elements.get(flows.get(id)[1]);
	}
	
	
	
	
	
	/**
	 * Creates a new SCCreator from an existing SC xml file
	 * @param scPath to xml file
	 * @param bpmnPath path to bpmn file
	 * @return new SCCreator
	 * @throws JDOMException when errors occur in parsing
	 * @throws IOException when an I/O error prevents a document from being fully parsed
	 */
	public static SimulationConfigurationCreator createFromFile(String scPath, String bpmnPath) throws JDOMException, IOException{
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(scPath);
        Element r = doc.getRootElement();
       	return new SimulationConfigurationCreator(r,doc);
	}
	
	/**
	 * Saves the document to a given path
	 * @param path
	 * @throws IOException
	 */
	public void save(String path) throws IOException{
		FileWriter writer = null;
		try{
			writer = new FileWriter(path);
	        XMLOutputter outputter = new XMLOutputter();
	        outputter.setFormat(Format.getPrettyFormat());
	        outputter.output(doc, writer);
		}finally{
	        writer.close();
		}
	}
	
}
