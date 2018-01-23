package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;

/**
 * Task element wrapper class
 * @author Leon Bein
 *
 */
public class Task extends ElementLink{

	/**Duration element, contains distribution and time unit*/
	private Element duration;
	/**Wrapper for duration distribution element*/
	private Distribution durationDistribution;
	/**Superelement for resource assignments*/
	private Element resourcesElement;
	/**Map containing all resource assignment wrappers and their resource ids*/
	private Map<String,ResourceAssignment> resources;
	
	/**
	 * Link constructor, links new object to existing task element, 
	 * parses distribution element and all assignment elements,
	 * searches for corresponding GC resource types, if a GCC is specified (can also be set later)
	 * @param toLink : Element to link with
	 * @param gcc : GCC for resource type lookup
	 */
	protected Task(Element toLink,GlobalConfigurationCreator gcc) {
		super(toLink);
		duration = el.getChild("duration",nsp);
		//Does it make sense for a subprocess to have a duration distribution?
		if(duration != null)durationDistribution = Distribution.create(duration.getChildren().get(0));
		resourcesElement = el.getChild("resources", nsp);
		resources = new TreeMap<String, ResourceAssignment>();
		if(resourcesElement != null)for(Element res : resourcesElement.getChildren("resource", nsp)){
			ResourceType type = gcc != null ? gcc.getResourceType(res.getAttributeValue("id")) : null;
			ResourceAssignment r = new ResourceAssignment(res,type);
			resources.put(r.getId(), r);
		}
	}
	
	/**
	 * Constructor, creates a new object and element, 
	 * creates a new empty duration element with default timeunit seconds
	 * and creates a new empty resources Element.
	 * Should match an element from the process model
	 * @param id : Id of the task in the process model
	 * @param name : Name of the task in the process model
	 */
	public Task(String id, String name) {
		super(new Element("Task",stdNsp));
		setAttribute("id",id);
		setAttribute("name",name);

		duration = new Element("duration",nsp);
		el.addContent(duration);
		setDurationTimeUnit(TimeUnit.SECONDS);	
		
		resourcesElement = new Element("resources", nsp);
		el.addContent(resourcesElement);
		resources = new TreeMap<String, ResourceAssignment>();
	}
	
	/**
	 * Subsequently parses the gcc resource types into the resource assignments.
	 * @param gcc : A not null, matching GCC
	 */
	public void setGCC(GlobalConfigurationCreator gcc) {
		for(ResourceAssignment a : resources.values()) {
			ResourceType type = gcc.getResourceType(a.getId());
			if(type == null) {
				System.err.println("Resource type definiton for Id "+a.getId()+" not found, cannot set assignment type.");
			}else {
				a.setResourceType(type);
			}
		}
	}
	
	/**
	 * Sets the time unit for the duration element
	 * @param t : Time unit to be set
	 */
	public void setDurationTimeUnit(TimeUnit t) {
		if(duration == null) {
			duration = new Element("duration",nsp);
			el.addContent(duration);
		}
		duration.setAttribute("timeUnit",t.toString());
	}
	/**
	 * @return the time unit string value of the duration element if existing, otherwise null
	 */
	public String getDurationTimeUnit(){
		if(duration == null)return null;
		return duration.getAttributeValue("timeUnit");
	}
	
	/**
	 * Sets the duration distribution; removes any existing distribution
	 * @param d : Wrapper for element to be set as distribution
	 */
	public void setDurationDistribution(Distribution d){
		if(duration == null) {
			duration = new Element("duration",nsp);
			el.addContent(duration);
		}
		if(durationDistribution != null){
			duration.removeContent();
		}
		durationDistribution = d;
		d.addTo(duration);
	}
	
	/**
	 * @return wrapper object for duration rate distribution, if already specified
	 */
	public Distribution getDurationDistribution(){
		return durationDistribution;
	}
	
	/**
	 * Returns the resource assignment for a given id.
	 * @param id : Id of the resource whose assignment shall be returned
	 * @return Wrapper for assignment element with matching id or null if none exists
	 */
	public ResourceAssignment getResource(String id){
		return resources.get(id);
	}
	
	/**
	 * @return all resource type ids that assignments are stored for
	 */
	public Set<String> getResources(){
		return resources.keySet();
	}
	
	/**
	 * Creates an assignment for a given resource type
	 * @param t : The resource type to be assigned
	 * @return An existing assignment with matching resource type, if on exists, otherwise a new one with amount 1
	 */
	public ResourceAssignment assignResource(ResourceType t){
		String id = t.getId();
		ResourceAssignment r = resources.get(id);
		if(r != null)return r;//if already assigned => return 
		r = new ResourceAssignment(t);
		r.addTo(resourcesElement);
		resources.put(id,r);
		return r;
	}
	
	/**
	 * Deassigns the resource type with given id if existing
	 * @param id : Id of resource type to be removed
	 */
	public void deassignResource(String id){
		ResourceAssignment r = resources.get(id);
		resources.remove(id);
		if(r != null)r.removeFrom(resourcesElement);
	}

}
