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
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;

/**
 * Main class for editing simulation configurations via java code. 
 * Parses existing SCs or creates new ones, a matching process model and global configuration file 
 * are required for best functioning.
 * @author Leon Bein
 *
 */
public class SimulationConfigurationCreator extends ElementLink{
	
	/**Error type thrown when trying to import an existing sc file and failing because of invalidity*/
	public static class NotAValidFileException extends Exception{
		private static final long serialVersionUID = -5836469671852200479L;
		private NotAValidFileException(String string) {
			super(string);
		}
	};
	/**Error type thrown when no process is specified inside a parsed process model file*/
	public static class NoProcessSpecifiedException extends Exception{
		private static final long serialVersionUID = -1868539111536676772L;
		private NoProcessSpecifiedException() {
			super("No process was found in the business model file");
		}
	}
	/**Error type thrown when the process model file does not specify a process with matching id, but overwriting is not authorized*/
	public static class NotAuthorizedToOverrideException extends Exception{
		private static final long serialVersionUID = -1047377004376282555L;
		private String newRef;
		private String oldRef;
		private NotAuthorizedToOverrideException(String ref, String newref) {
			super("Process matching to id "+ref+" not found and not allowed to override with "+newref+" . Call this method again with set override flag to override.");
			newRef = newref;
			oldRef = ref;
		}
		public String getNewRef() {return newRef;}
		public String getOldRef() {return oldRef;}
	}

	/**jdom2 XML document object of the global configuration*/
	private Document doc;
	/**Root object of document*/
	private Element root;
	
	/**Start event element*/
	private StartEvent startEvent;
	/**Map of parsed known elements like tasks, gateways, etc. and their ids*/
	private Map<String,ElementLink> elements;
	/**Map of sequence flows, containing their id as key, and the source and target id as array as value*/
	private Map<String,String[]> flows;
	
	/**A matching global configuration creator, needed in order to determine resources*/
	private GlobalConfigurationCreator gcc;
	
	/**
	 * Public constructor,
	 * generates new empty SimulationConfiguration, linked with new simulationConfiguration element
	 */
	public SimulationConfigurationCreator(){
		super(new Element("simulationConfiguration", stdNsp));
		Element superroot = new Element("definitions",nsp);
		superroot.setAttribute("targetNamespace", "http://www.hpi.de");//Superrot element, document root
		doc = new Document(superroot);
		root = el;//actual "root" element for sc creating purpose
		superroot.addContent(root);
		elements = new TreeMap<String,ElementLink>();
		flows = new HashMap<String,String[]>();
		//There shall be no startevent, that has a non-matching id.
//		startEvent = new StartEvent("startEvent");
//		startEvent.addTo(this);
//		elements.put(startEvent.getId(),startEvent);
	}
	
	/**
	 * Link constructor, links with given sc-root element and document, imports existing definitions
	 * @param r : Sc root element to link with and import
	 * @param d : xml document object, needed for fileIO later
	 */
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

	/**Sets id of the simulation configuration*/
	public void setId(String id){setAttribute("id", id);}
	/**@return id of the simulation configuration*/
	public String getId(){return root.getAttributeValue("id");}
	/**Sets which process id inside a bpmn diagram is linked to this sc*/
	private void setProcessRef(String processRef){setAttribute("processRef", processRef);}
	/**@return which process id inside a bpmn diagram is linked to this sc*/
	private String getProcessRef(){return root.getAttributeValue("processRef");}
	/**Sets the number of process instances that are simulated with this configuration*/
	public void setProcessInstances(int instances){setAttribute("processInstances", instances);}
	/**@return the number of process instances that are simulated with this configuration*/
	public String getProcessInstances(){return root.getAttributeValue("processInstances");}
	/**Sets the simulated start date and time of this configuration*/
	public void setStartDateTime(ZonedDateTime startTime){setAttribute("startDateTime", startTime);}
	/**@return the simulated start date and time of this configuration*/
	public String getStartDateTime(){return root.getAttributeValue("startDateTime");}
	/**Sets the simulated end date and time of this configuration*/
	public void setEndDateTime(ZonedDateTime endTime){setAttribute("endDateTime", endTime);}
	/**@return the simulated end date and time of this configuration*/
	public String getEndDateTime(){return root.getAttributeValue("endDateTime");}
	/**Removes the end time element, so that the simulation is only limited by when the instances are finished and not by simulation time*/
	public void removeEndDateTime(){root.removeAttribute("endDateTime");}
	//TODO public void setResourceAssignmentOrder(String id){setAttribute("resourceAssignmentOrder", id);}
	/**Sets the random seed of this sc, if not set, the global seed will be used*/
	public void setRandomSeed(long seed){
		root.setAttribute("randomSeed",seed+"");
	}
	/**Removes the random seed element of this sc, so the global seed is used*/
	public void removeRandomSeed(){root.removeAttribute("randomSeed");}
	/**@return the random seed of this sc if existing, null otherwise*/
	public Long getRandomSeed(){
		if(root.getAttributeValue("randomSeed") == null)return null;
		return Long.valueOf(root.getAttributeValue("randomSeed"));
	}
	/**@return the wrapper object for this sc's startevent element*/
	public StartEvent getStartEvent(){return startEvent;}
	/**sets the wrapper object for this sc's startevent element*/
	private void setStartEvent(StartEvent e) {startEvent = e;}

	/**
	 * Links a process model to this sc.
	 * If a process id is specified and the corresponding process definition is found inside the pm file,
	 * this process is used otherwise the first definition is taken. If no process is defined, a warning
	 * will be printed, but no error or exception occurs!
	 * Also removes existing parsed elements, that are not defined inside the pm file
	 * @param modelRoot : Root element of the process model
	 * @param override : Specifies, whether or not to override existing process references
	 * @throws NoProcessSpecifiedException : When not process is specified inside the file
	 * @throws NotAuthorizedToOverrideException  : When no matching process is specified inside the file and override flag is not set;
	 *  call this method with set override flag to override
	 */
	public void setModel(Element modelRoot, boolean override) throws NoProcessSpecifiedException, NotAuthorizedToOverrideException{
		List<Element> processes = modelRoot.getChildren("process",modelRoot.getNamespace());
		if(processes.size() == 0){
			throw new NoProcessSpecifiedException();
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
			if(process == null){
				process = processes.get(0);
				if(!override)throw new NotAuthorizedToOverrideException(ref,process.getAttributeValue("id"));
				setProcessRef(process.getAttributeValue("id"));
				System.err.println("Warning: Process matching to id "+ref+" not found, overriding with "+getProcessRef()+" instead.");
			}
		}

		addProcessModelElements(process,this);
		
		//Collect elements that shall be removed and remove them afterwards to avoid exceptions
		ArrayList<String> toRem = new ArrayList<String>();
		outer:for(String id : elements.keySet()) {
			for(Element child : process.getChildren()){
				if(id.equals(child.getAttributeValue("id")))continue outer;
			}
			toRem.add(id);
		}
		for(String id : toRem)elements.remove(id);

	}
	
	/**
	 * Sets the corresponding gcc for this sc and all tasks as they need it for resource management
	 * @param g : A global configuration creator object
	 */
	public void setGCC(GlobalConfigurationCreator g){
		gcc = g;
		for(ElementLink task : getElements()) {
			if(task instanceof Task) {
				((Task) task).setGCC(gcc);
			}
		}
	}
	
	/**
	 * Tries to parse all elements of a given process and add the results into the current sc element structure
	 * @param process : Root of the process, all children will be parsed if possible
	 * @param addTo : Current sc context, normally sc root or a subprocess
	 */
	public void addProcessModelElements(Element process, ElementLink addTo){
		for(Element child : process.getChildren()){
			if(isKnownModelElement(child.getName()))
				createModelElement(child, addTo);
		}
	}
	
	/**
	 * Parses a process model element to a new sc element with wrapper object
	 * @param child : Process model element to be parsed (e.g. Task)
	 * @param addTo : Context for the new sc element to be added to (normally sc root or a subprocess)
	 */
    private void createModelElement(Element child, ElementLink addTo) {
    	String id = child.getAttributeValue("id");
    	//Don't add elements that are already parsed (e.g when loading an existing, incomplete SC)
    	if(id != null && ! id.isEmpty() && elements.containsKey(id))return;
		String name = child.getName();
		switch(name){
		case "startEvent" :
			if(!addTo.equals(this))break;//ignore subProcess startEvents
			setStartEvent(new StartEvent(id));
			getStartEvent().addTo(addTo);
			elements.put(getStartEvent().getId(),getStartEvent());
			break;
			
		case "subProcess" :
			SubProcess s = new SubProcess(id,child.getAttributeValue("name"));
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

			if(branchids.size() <= 1) {
				elements.put(id,new ElementLink(child) {});
				break;//No branching probabilities for a join gateway - no definition needed
			}
			ExclusiveGateway eg = new ExclusiveGateway(id,branchids);
			elements.put(eg.getId(),eg);
			eg.addTo(addTo);
			break;
			
		case "sequenceFlow" :
			flows.put(id, new String[]{child.getAttributeValue("sourceRef"),child.getAttributeValue("targetRef")});
			break;
			
		default :
			if(name.equals("task") || name.endsWith("Task")){
				Task t = new Task(id,child.getAttributeValue("name"));
				t.addTo(addTo);
				elements.put(t.getId(),t);
			} else if(name.endsWith("Event")) {
				elements.put(id, new ElementLink(child) {});
			}
			break;
		}
	}
    
    /**
     * Imports existing sc elements into  new wrapper objects
     * @param root : Root of the elements to parse
     */
    private void addProcessSCElements(Element root){
		for(Element e : root.getChildren()){
			String name = e.getName();
			if(isKnownSCElement(name)){
				/*        return name.equals("task") || name.endsWith("Task") || name.equals("startEvent") || name.equals("subProcess")
                || name.equals("resources");*/
				if(name.equals("startEvent")){
					setStartEvent(new StartEvent(e));
					elements.put(getStartEvent().getId(),getStartEvent());
				}else if(name.equals("task") || name.endsWith("Task")){
					Task t = new Task(e,gcc);
					elements.put(t.getId(), t);
				}else if(name.equals("subProcess")){
					SubProcess p = new SubProcess(e,gcc);
					elements.put(p.getId(), p);
					addProcessSCElements(e);
				}else if(name.equals("exclusiveGateway")){
					ExclusiveGateway g = new ExclusiveGateway(e);
					elements.put(g.getId(),g);
				}
			}
		}
    }

    
    /**
     * @return Whether a process model element of given type name can be parsed
     * @see {@link de.hpi.bpt.scylla.parser.ProcessModelParser#isKnownElement(String name)}
     */
	private boolean isKnownModelElement(String name) {
	      return name.equals("task") || name.endsWith("Task") || name.endsWith("Event")
	      || name.endsWith("Gateway") || name.equals("subProcess")
	      || name.equals("sequenceFlow") || name.equals("startEvent");
//        return name.equals("sequenceFlow") || name.equals("task") || name.endsWith("Task") || name.endsWith("Event")
//                || name.endsWith("Gateway") || name.equals("subProcess") || name.equals("callActivity")
//                || name.equals("laneSet") || name.equals("dataObjectReference") || name.equals("ioSpecification")
//                || name.equals("dataObject");
    }
	
	/**
	 * @return whether a simulation configuration element of given type name can be parsed
	 * @see {@link de.hpi.bpt.scylla.parser.SimulationConfigurationParser#isKnownElement(String name)}
	 */
	private boolean isKnownSCElement(String name){
        return name.equals("task") || name.endsWith("Task") || name.equals("startEvent") || name.equals("subProcess")
                || name.equals("resources") || name.equals("exclusiveGateway");
	}
	
	
	/**
	 * Returns the element with a given id
	 * @param id : Id attribute value of the element
	 * @return Wrapper object of different classes
	 */
	public ElementLink getElement(String id){
		return elements.get(id);
	}
	
	/**
	 * @return list of wrappers of all parsed elements
	 */
	public Collection<ElementLink> getElements(){
		return elements.values();
	}

	/**
	 * @param id The id of a sequence flow
	 * @return The id of the process element that is targeted by a given sequence flow
	 */
	public ElementLink getFlowSource(String id){
		return elements.get(flows.get(id)[0]);
	}

	/**
	 * @param id The id of a sequence flow
	 * @return The id of the processs element that is the source of a given sequence flow
	 */
	public ElementLink getFlowTarget(String id){
		return elements.get(flows.get(id)[1]);
	}
	
	
	
	
	
	/**
	 * Creates a new SCCreator from an existing SC xml file
	 * @param scPath to xml file
	 * @return new SCCreator
	 * @throws JDOMException when errors occur in parsing
	 * @throws IOException when an I/O error prevents a document from being fully parsed
	 * @throws NotAValidFileException a) when the file has no simulation configuration root element
	 */
	public static SimulationConfigurationCreator createFromFile(String scPath) throws JDOMException, IOException, NotAValidFileException{
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(scPath);
        Element r = doc.getRootElement();
        if(r.getChild("simulationConfiguration",stdNsp) == null)throw new NotAValidFileException("Cannot open simulation configuration with path "+scPath+" : root element not found");
        SimulationConfigurationCreator sc = new SimulationConfigurationCreator(r,doc);
        return sc;
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
