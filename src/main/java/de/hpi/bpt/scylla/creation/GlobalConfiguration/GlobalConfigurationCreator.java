package de.hpi.bpt.scylla.creation.GlobalConfiguration;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import de.hpi.bpt.scylla.creation.ElementLink;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType.ResourceInstance;

/**
 * Class for creating and editing global configurations
 * @author Leon Bein
 *
 */
public class GlobalConfigurationCreator extends ElementLink{
	
	/**jdom2 XML document object of the global configuration*/
	private Document doc;
	/**Root object of document*/
	private Element root;

	/**List of all resource types*/
	private List<ResourceType> resourceTypes;
	/**List of all timetables*/
	private List<Timetable> timetables;


	/**
	 * Public constructor,
	 * generates new empty GlobalConfiguration
	 */
	public GlobalConfigurationCreator(){
		super(new Element("globalConfiguration",stdNsp));
		root = el;
		doc = new Document(root);
		root.setAttribute("targetNamespace", "http://www.hpi.de");

		resourceTypes = new ArrayList<ResourceType>();
		timetables = new ArrayList<Timetable>();
	}
	
	/**
	 * Getter for {@link 
Document de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.doc}
	 * @return jdom2 Document
	 */
	public Document getDoc() {
		return doc;
	}


	/**
	 * Sets the id of the global configuration
	 * TODO replace with setting filename? 
	 * @param id
	 */
	public void setId(String id){
		setAttribute("id",id);
	}
	
	/**
	 * Creates the resourceAssignmentOrder element if not existing and returns it
	 * @return resourceAssignmentOrder element
	 */
	private Element getResourceAssignmentOrder(){
		if(root.getChild("resourceAssignmentOrder",nsp) == null){
			Element resourceAssignmentOrder = new Element("resourceAssignmentOrder",nsp);
			root.addContent(resourceAssignmentOrder);
			return resourceAssignmentOrder;
		}else{
			return root.getChild("resourceAssignmentOrder",nsp);
		}
	}
	
	/**
	 * Adds a resource assignment order by adding its id and a comma to the String
	 * TODO check if plugin is existing? give warning?
	 * @param ao resource assignment id
	 */
	public void addReferencedResourceAssignmentOrder(String ao){
		Element resourceAssignmentOrder = getResourceAssignmentOrder();
		resourceAssignmentOrder.setText(resourceAssignmentOrder.getText()+ao+",");
	}	
	
	/**
	 * Removes a resource assignment order 
	 * @param ao resource assignment id
	 */
	public void removeReferencedResourceAssignmentOrder(String ao){		
		Element resourceAssignmentOrder = getResourceAssignmentOrder();
		resourceAssignmentOrder.setText(resourceAssignmentOrder.getText().replace(ao+",",""));
	}
	
	/**
	 * Sets the GCs seed to a given value
	 * @param seed seed as long value
	 */
	public void setSeed(long seed){
		if(root.getChild("randomSeed",nsp) == null)root.addContent(new Element("randomSeed",nsp));
		root.getChild("randomSeed",nsp).setText(seed+"");
	}
	
	/**
	 * Sets the GCs time offset 
	 * @param timezone TimeZone, given as zoneoffset to greenwich mean time
	 */
	public void setTimeOffset(ZoneOffset timezone){
		if(root.getChild("zoneOffset",nsp) == null)root.addContent(new Element("zoneOffset",nsp));
		root.getChild("zoneOffset",nsp).setText(timezone+"");
	}
	
	
	
	/**
	 * Handler class for resource types
	 * @author Leon Bein
	 *
	 */
	public class ResourceType extends ElementLink{
		
		/**Parent object*/
		public final GlobalConfigurationCreator globalConfigurationCreator = GlobalConfigurationCreator.this;
		
		/**Unique identifier*/
		private String id;
		
		/**List of instance handler objects*/
		private List<ResourceInstance> resourceInstances;
		
		/**
		 * Constructor for creating new ResourceTypes or new GCs in general
		 * @param i Unique identifier string
		 */
		private ResourceType(String i){
			super(new Element("dynamicResource",GlobalConfigurationCreator.this.nsp));
			id = i;
			setAttribute("id",id);
			getResourceData().addContent(el);
			resourceInstances = new ArrayList<ResourceInstance>();
		};
		
		/**
		 * Constructor for linking a new handler to an already existing xml element or an existing GC in general
		 * @param e xml element to be linked with
		 */
		private ResourceType(Element e){
			super(e);
			id = el.getAttributeValue("id");
			resourceInstances = new ArrayList<ResourceInstance>();
		}
		


		/**Resets the id to a new value, can cause invalidity by duplicate*/
		public void setId(String id) {
			setAttribute("id",id);
			this.id = id;
		}
		/**Sets the resource types display name attribute (!= id)*/
		public void setName(String name){setAttribute("name", name);}
		/**Sets the resource types default quantity of instances*/
		public void setDefaultQuantity(int dq){setAttribute("defaultQuantity", dq);}
		/**Sets the resource types default cost*/
		public void setDefaultCost(double dk){setAttribute("defaultCost", dk);}
		/**Sets the resource types default time unit; only accepts  {@link java.util.concurrent.TimeUnit} objects*/
		public void setDefaultTimeUnit(TimeUnit tu){setAttribute("defaultTimeUnit", tu);}
		/**Sets the resource types default timetable identifier, can cause invalidity by setting to a non-existing id*/
		public void setDefaultTimetableId(String ttid){setAttribute("defaultTimetableId", ttid);}
		/**Removes the resources types default timetable identifier, possible as it is optional*/
		public void removeDefaultTimetableId(){el.removeAttribute("defaultTimetableId");}
		
		/**Returns the resource types identifier*/
		public String getId() {return id;}
		/**Returns the resource types display name*/
		public String getName(){return el.getAttributeValue("name");}
		/**Returns the resource types default quantity of instances*/
		public String getDefaultQuantity(){return el.getAttributeValue("defaultQuantity");}
		/**Returns the resource types default cost per instance*/
		public String getDefaultCost(){return el.getAttributeValue("defaultCost");}
		/**Returns the resource types default timeunit, as String, not as timeunit object*/
		public String getDefaultTimeUnit(){return el.getAttributeValue("defaultTimeUnit");}
		/**Returns the resource types default timetable identifier*/
		public String getDefaultTimetableId(){return el.getAttributeValue("defaultTimetableId");}
		
		/**Handler class for resource instances*/
		public class ResourceInstance extends ElementLink{
			
			/**Reference to parent resourcetype object*/
			public final ResourceType resourceType = ResourceType.this;
			
			/**Instance name, unique identifier*///TODO will #1 crash the system?
			private String name;
			
			/**
			 * Constructor for creating new ResourceInstances or new GCs in general
			 * @param n Unique name string
			 */
			private ResourceInstance(String n){
				super(new Element("instance",ResourceType.this.nsp));
				name = n;
				setName(name);
				resourceType.el.addContent(el);
			}
			
			/**
			 * Constructor for linking a new handler to an already existing xml element or an existing GC in general
			 * @param e xml element to be linked with
			 */
			private ResourceInstance(Element e){
				super(e);
				name = getName();
			}

			/**Sets instances unique name, can cause invalidity if not unique*/
			public void setName(String name){setAttribute("name", name);}
			/**Sets instances cost*/
			public void setCost(double dk){setAttribute("cost", dk);}
			/**Removes instances cost, if not specified, default resource type cost will be used*/
			public void removeCost(){el.removeAttribute("cost");}
			/**Sets instances time unit*/
			public void setTimeUnit(TimeUnit tu){setAttribute("timeUnit", tu);}
			/**Removes instances time unit, if not specified,resource type default will be used*/
			public void removeTimetUnit(){el.removeAttribute("timeUnit");}
			/**Sets instances timetable identifier, can cause invalidity, if timetable is not existing*/
			public void setTimetableId(String ttid){setAttribute("timetableId", ttid);}
			/**Removes instances timetableid, if not specified, resource type default will be used (if specified there)*/
			public void removeTimetableId(){el.removeAttribute("timetableId");}
			
			/**Returns instances name*/
			public String getName(){return el.getAttributeValue("name");}
			/**Returns instances cost as string, returns null if not specified*/
			public String getCost(){return el.getAttributeValue("cost");}
			/**Returns instances time unit as string, returns null if not specified*/
			public String getTimeUnit(){return el.getAttributeValue("timeUnit");}
			/**Returns instances time table identifier, returns null if not specified*/
			public String getTimetableId(){return el.getAttributeValue("timetableId");}

		}
		
		/**
		 * Returns instance object for given name
		 * @param name instance name
		 * @return instance handler object, or null if none exists
		 */
		public ResourceInstance getInstance(String name){
			for(ResourceInstance res : resourceInstances){
				if(res.name.equals(name))return res;
			}
			return null;
		}
		
		/**
		 * Adds an instance to this resource type, doesn't add it, if name already exists
		 * @param name unique name of new resource
		 * @return returns the created instance or the already existing instance with given name
		 */
		public ResourceInstance addInstance(String name){
			ResourceInstance inst = getInstance(name);
			if(inst == null){
				inst = new ResourceInstance(name);
				resourceInstances.add(inst);
			}
			return inst;
		}
		
		
		
		/**
		 * Removes an instance with given name, if existing
		 * @param name unique name of instance to remove
		 */
		public void removeInstance(String name){
			ResourceInstance toRem = getInstance(name);
			if(toRem == null)return;
			toRem.removeFrom(el);
			//el.removeContent(toRem.getEl());
			resourceInstances.remove(toRem);
		}
		
	}
	
	/**
	 * Creates a the resourceData element, if not existing, and returns it
	 * @return jdom xml element pointing to resourceData
	 */
	private Element getResourceData() {
		if(root.getChild("resourceData",nsp) == null){
			Element resourceData = new Element("resourceData",nsp);
			root.addContent(resourceData);
			return resourceData;
		}else{
			return root.getChild("resourceData",nsp);
		}
	}
	
	/**
	 * Finds the corresponding resource type handler object to given id
	 * @param id unique identifier string for resource type
	 * @return the handler if exitsting, otherwise null
	 */
	public ResourceType getResourceType(String id){
		for(ResourceType res : resourceTypes){
			if(res.id.equals(id))return res;
		}
		return null;
	}
	
	/**
	 * Creates a new resource type for given id, if the id is available
	 * @param id unique identifier of object to create
	 * @return resource type handler object for given id, newly created, or already existing
	 */
	public ResourceType addResourceType(String id){
		ResourceType res = getResourceType(id);
		if(res == null){
			res = new ResourceType(id);
			resourceTypes.add(res);
		}//else duplicate
		return res;
	}
	
	/**
	 * Removes a resource type with given id, if exising
	 * @param id unique identifier string for resource type to remove
	 */
	public void removeResourceType(String id){
		Element resourceData = getResourceData();
		ResourceType toRem = getResourceType(id);
		if(toRem == null)return;
		//resourceData.removeContent(toRem.getEl());
		toRem.removeFrom(resourceData);
		resourceTypes.remove(toRem);
	}
	
	
	/**
	 * Handler class for timetable objects
	 * @author Leon Bein
	 *
	 */
	public class Timetable extends ElementLink{
		/**Reference to parent GCCreator object*/
		public final GlobalConfigurationCreator globalConfigurationCreator = GlobalConfigurationCreator.this;
		/**Unique identifier String*/
		private String id;

		/**List of all timetableitems*/
		private List<TimetableItem> items;
		

		

		/**
		 * Constructor for creating new Timetables or new GCs in general
		 * @param i Unique identifier string
		 */
		private Timetable(String i){
			super(new Element("timetable",GlobalConfigurationCreator.this.nsp));
			id = i;
			setAttribute("id",id);
			getTimetables().addContent(el);
			items = new ArrayList<TimetableItem>();
		}
		
		/**
		 * Constructor for linking a new handler to an already existing xml element or an existing GC in general
		 * @param e xml element to be linked with
		 */
		private Timetable(Element e){
			super(e);
			id = el.getAttributeValue("id");
			items = new ArrayList<TimetableItem>();
		}
		
		/**Handler class for timetable items*/
		public class TimetableItem extends ElementLink{
			
			/**Reference to parent timetable object*/
			public final Timetable timetable = Timetable.this;
			
			/**
			 * Constructor for creating new timetable items or new GCs in general
			 * @param from start day
			 * @param to end day
			 * @param beginTime daily start time
			 * @param endTime daily end time
			 */
			private TimetableItem(DayOfWeek from, DayOfWeek to, LocalTime beginTime, LocalTime endTime){
				super(new Element("timetableItem",stdNsp));
				setAttribute("from", from);
				setAttribute("to", to);
				setAttribute("beginTime", beginTime);
				setAttribute("endTime", endTime);
				timetable.el.addContent(el);
				timetable.items.add(this);
			}
			
			/**
			 * Constructor for linking a new handler to an already existing xml element or an existing GC in general
			 * @param e xml element to be linked with
			 */
			private TimetableItem(Element e){
				super(e);
			}
			
			/**Sets items start day*/
			public void setFrom(DayOfWeek d){setAttribute("from", d);}
			/**Returns items start day as string*/
			public String getFrom(){return el.getAttributeValue("from");}
			/**Sets items end day*/			
			public void setTo(DayOfWeek d){setAttribute("to", d);}
			/**Returns items end day as string*/			
			public String getTo(){return el.getAttributeValue("to");}
			/**Sets items daily start time*/
			public void setBeginTime(LocalTime l){setAttribute("beginTime", l);}
			/**Returns items daily start time as string*/
			public String getBeginTime(){return el.getAttributeValue("beginTime");}
			/**Sets items daily end time*/
			public void setEndTime(LocalTime l){setAttribute("endTime", l);}
			/**Returns items daily end time as string*/
			public String getEndTime(){return el.getAttributeValue("endTime");}

			/**
			 * Validates the timetableitem; checks if all data attributes are specified and if all data types are correct
			 * @param errors List to add errors to
			 * @param warnings List to add warnings to
			 * @param index index of this element, as it is dynamic and the element doesn't know it; to make errors be more helpful
			 */
			public void validate(List<String> errors, List<String> warnings, int index) {
				if(getFrom() == null)errors.add("Start day of item "+index+" from table "+timetable.id+" not specified");
				else try{ 
					DayOfWeek.valueOf(getFrom());
				}catch(IllegalArgumentException e){
					errors.add("Start day of item "+index+" from table "+timetable.id+" not a valid day of week");
				}
				if(getTo() == null)errors.add("End day of item "+index+" from table "+timetable.id+" not specified");
				else try{ 
					DayOfWeek.valueOf(getTo());
				}catch(IllegalArgumentException e){
					errors.add("End day of item "+index+" from table "+timetable.id+" not a valid day of week");
				}
				if(getBeginTime() == null)errors.add("Start time of item "+index+" from table "+timetable.id+" not specified");
				else try{
					LocalTime.parse(getBeginTime());
				}catch(DateTimeParseException e){
					errors.add("Start time of item "+index+" from table "+timetable.id+" is not a time.");
				}
				if(getEndTime() == null)errors.add("End time of item "+index+" from table "+timetable.id+" not specified");
				else try{
					LocalTime.parse(getEndTime());
				}catch(DateTimeParseException e){
					errors.add("End time of item "+index+" from table "+timetable.id+" is not a time.");
				}
			}
			
		}
		
		/**
		 * Adds new item to this timetable
		 * @param from the items start day
		 * @param to the items end day
		 * @param beginTime the items daily start time
		 * @param endTime the items daily end time
		 * @return
		 */
		public TimetableItem addItem(DayOfWeek from, DayOfWeek to, LocalTime beginTime, LocalTime endTime){
				TimetableItem i = new TimetableItem(from, to, beginTime, endTime);
				items.add(i);
				return i;
		}
		
		/**
		 * Returns item at given index, may throw error if out of bounds <br>
		 * (timetable items have no unique identifier except this index)
		 * @param index index of item in list
		 * @return timetable item handler with given index
		 */
		public TimetableItem getItem(int index){
			return items.get(index);
		}
		/**
		 * Removes item of given index, may throw indexoutofbounds error
		 * @param index index of element in item list
		 */
		public void removeItem(int index){
			TimetableItem item = getItem(index);
			removeItem(item);
		}
		/**
		 * Removes given item from list and document
		 * @param i item to be removed
		 */
		public void removeItem(TimetableItem i){
			//el.removeContent(i.getEl());
			i.removeFrom(el);
			items.remove(i);
		}
		
		/**
		 * Returns the number of items in this timetable
		 * @return {@link java.util.List.size()}
		 */
		public int getNumItems(){
			return items.size();
		}
		
		/**
		 * @return Unique identifier of this timetable
		 */
		public String getId() {
			return id;
		}

		/**
		 * Validates this timetable and its items; logs errors and warnings in lists
		 * @param errors list to log errors
		 * @param warnings list to log warnings
		 */
		public void validate(List<String> errors, List<String> warnings) {
			if(items.isEmpty())warnings.add("No time interval defined for timetable with id "+id);
			//validate timetableitems
			for(int j = 0; j < items.size(); j++){
				items.get(j).validate(errors,warnings,j);
			}
			
		}
		
		
	}
	
	/**
	 * Creates, if not existing, and returns the GCs timetables element
	 * @return the jdom2 xml element corresponding to the timetables item of the GC
	 */
	private Element getTimetables() {
		if(root.getChild("timetables",nsp) == null){
			Element resourceData = new Element("timetables",nsp);
			root.addContent(resourceData);
			return resourceData;
		}else{
			return root.getChild("timetables",nsp);
		}
	}
	
	/**
	 * Finds the timetable with given id
	 * @param id unique identifier string of timetable
	 * @return timetable handler object for the timetable
	 */
	public Timetable getTimetable(String id){
		for(Timetable tt : timetables){
			if(tt.id.equals(id))return tt;
		}
		return null;
	}
	
	/**
	 * Creates a new timetable with given id, if none exisists with this id yet
	 * @param id unique id of timetable
	 * @return timetable object with given id, new or existing one
	 */
	public Timetable createTimetable(String id){
		Timetable t = getTimetable(id);
		if(t == null){
			t = new Timetable(id);
			timetables.add(t);
		}//else duplicate
		return t;
	}
	
	/**
	 * Deletes a timetable with given id if existing
	 * @param id unique timetable identifier string
	 */
	public void deleteTimetable(String id){
		Element ts = getTimetables();
		Timetable toRem = getTimetable(id);
		if(toRem == null)return;
		//ts.removeContent(toRem.getEl());
		toRem.removeFrom(ts);
		timetables.remove(toRem);
	}
	
	
	
	/**
	 * Validates the whole GC;
	 * It is valid if no known errors occured, but may still have warnings
	 * @return if the GC is valid or not
	 */
	public boolean validate(){
		List<String> errors = new ArrayList<String>();
		List<String> warnings = new ArrayList<String>();
		
		//validate Resources
		for(int i = 0; i < resourceTypes.size(); i++){
			ResourceType resType = resourceTypes.get(i);
			//validate id 
			for(int j = i+1; j < resourceTypes.size(); j++){
				if(resourceTypes.get(j).id.equals(resType.id))errors.add("Duplicate resource type identifier: "+resType.id);
			}
			
			//validate defaultQuantity
			String defaultQuantity = resType.getDefaultQuantity();
			if(defaultQuantity == null){
				errors.add("Default quantity of resource type with id "+resType.id+" not specified");
			}
			else try{
				if(Integer.parseInt(defaultQuantity) <= 0){
					if(Integer.parseInt(defaultQuantity) == 0)warnings.add("Default quantity of resource type with id "+resType.id+" is zero");
					else errors.add("Default quantity of resource type with id "+resType.id+" is negative");
				}
			}catch(NumberFormatException e){
				errors.add("Default quantity of resource type with id "+resType.id+" must be an integer");
			}

			
			//Validate defaultCost
			if(resType.getDefaultCost() == null){
				errors.add("Default cost of resource type with id "+resType.id+" not specified");
			}
			else try{
				if(Double.parseDouble(resType.getDefaultCost()) < 0){
					warnings.add("Default cost of resource type with id "+resType.id+" is negative");
				}
			}catch(NumberFormatException e){
				errors.add("Default cost of resource type with id "+resType.id+" must be a number");
			}
			
			//Validate default time unit
			if(resType.getDefaultTimeUnit() == null){
				errors.add("Default time unit of resource type with id "+resType.id+" not specified");
			}else try{
				TimeUnit.valueOf(resType.getDefaultTimeUnit());
			}catch(IllegalArgumentException e){
				errors.add("Default time unit of resource type with id "+resType.id+" is not a valid time unit");
			}
			
			//Validate Timetableid
			String defTimetableId = resType.getDefaultTimetableId();
			if(defTimetableId != null){
				if(!validateTimetableId(defTimetableId))errors.add("Timetable Id "+defTimetableId+" of resource type with id "+resType.id+" is not valid.");
			}
			
			//Validate Instances
			for(int j = 0; j < resType.resourceInstances.size(); j++){
				ResourceInstance inst = resType.resourceInstances.get(j);
				//Validate name
				for(int k = j+1; k < resType.resourceInstances.size(); k++){
					if(resType.resourceInstances.get(k).name.equals(inst.name))errors.add("Duplicate instance name of type with id "+resType.id+" : "+inst.name);
				}
				//Validate instance costs
				if(inst.getCost() != null){
					try{
						if(Double.parseDouble(inst.getCost()) < 0){
							errors.add("Costs of instance "+inst.getName()+" of type "+resType.id+" are negative");
						}
					}catch(NumberFormatException e){
						errors.add("Costs of instance "+inst.getName()+" of type "+resType.id+" is not a number");
					}
				}
				//Validate instance timeTableid

				String timetableId = inst.getTimetableId();
				if(timetableId != null){
					if(!validateTimetableId(timetableId))errors.add("Timetable Id "+timetableId+" of instance "+inst.getName()+" of type with id "+resType.id+" is not valid.");
				}
			}
			
		}
		
		//validate Timetable
		for(int i = 0; i < timetables.size(); i++){
			Timetable table = timetables.get(i);
			for(int j = i+1; j < timetables.size(); j++){
				if(timetables.get(j).id.equals(table.id))errors.add("Duplicate timetable identifier: "+table.id);
			}
			table.validate(errors,warnings);
		}
		
		for(String s: errors)System.err.println("[Error] "+s);
		for(String s: warnings)System.err.println("[Warning] "+s);
		return errors.isEmpty();
	}
	
	/**
	 * Returns if a timetable with given id exists
	 * @param id unique identifier of timetable
	 * @return getTimetable(id) != null
	 */
	private boolean validateTimetableId(String id){
		return getTimetable(id) != null;
	}
	
	/**
	 * Constructor for linking a new GC handler to an existing xml file <br>
	 * "light version" of {@link GlobalConfiguration de.hpi.bpt.scylla.parser.GlobalConfigurationParser.parse(Element rootElement)}
	 * @param r root element of existing xml file
	 * @param d xml document
	 */
	private GlobalConfigurationCreator(Element r,Document d){
		super(r);
		nsp = Namespace.getNamespace("bsim","http://bsim.hpi.uni-potsdam.de/scylla/simModel");
		root = el;
		doc = d;
		root.setAttribute("targetNamespace", "http://www.hpi.de");

		resourceTypes = new ArrayList<ResourceType>();
		timetables = new ArrayList<Timetable>();
		
        for (Element el : root.getChildren(null,nsp)) {
            String elementName = el.getName();
            
            if (elementName.equals("resourceAssignmentOrder")) {}
            else if (elementName.equals("randomSeed")) {}
            else if (elementName.equals("zoneOffset")) {}
            else if (elementName.equals("resourceData")) {
                List<Element> rDataElements = el.getChildren("dynamicResource",nsp);
                for (Element elem : rDataElements) {
                	ResourceType type = new ResourceType(elem);
                	resourceTypes.add(type);
                    List<Element> instanceElements = elem.getChildren("instance", nsp);

                    // parse defined resource instances
                    for (Element inst : instanceElements) {
                    	type.resourceInstances.add(type.new ResourceInstance(inst));
                    }
                }
            }
            else if (elementName.equals("timetables")) {
                List<Element> tables = el.getChildren("timetable", nsp);
                for (Element table : tables) {
                	Timetable tab = new Timetable(table);
                	timetables.add(tab);
                    List<Element> tableItems = table.getChildren("timetableItem", nsp);
                    for (Element tableItem : tableItems) {
                        tab.items.add(tab.new TimetableItem(tableItem));
                    }
                }
            }

        }

	}
	
	/**
	 * Creates a new GCCreator from an existing GC xml file
	 * @param path to xml file
	 * @return new GCCreator
	 * @throws JDOMException when errors occur in parsing
	 * @throws IOException when an I/O error prevents a document from being fully parsed
	 */
	public static GlobalConfigurationCreator createFromFile(String path) throws JDOMException, IOException{
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(path);
        Element r = doc.getRootElement();
//        if(!doc.getNamespacesInScope().get(0).getURI().equals("http://bsim.hpi.uni-potsdam.de/scylla/simModel")){
//        	System.err.println("Error creating GlobalconfigurationCreator from path "+path+" - invalid namespace");
//        }
       	return new GlobalConfigurationCreator(r,doc);
	}
	
	
}
