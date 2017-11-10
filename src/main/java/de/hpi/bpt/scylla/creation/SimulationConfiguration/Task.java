package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;

public class Task extends ElementLink{

	private Element duration;
	private Distribution durationDistribution;
	private Element resourcesElement;
	private Map<String,ResourceAssignment> resources;
	
	/**
	 * Link constructor
	 * @param toLink
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
	

	public void setDurationTimeUnit(TimeUnit t) {
		if(duration == null) {
			duration = new Element("duration",nsp);
			el.addContent(duration);
		}
		duration.setAttribute("timeUnit",t.toString());
	}
	public String getDurationTimeUnit(){
		if(duration == null)return null;
		return duration.getAttributeValue("timeUnit");
	}
	
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
	
	public Distribution getDurationDistribution(){
		return durationDistribution;
	}
	
	public ResourceAssignment getResource(String id){
		return resources.get(id);
	}
	
	public Set<String> getResources(){
		return resources.keySet();
	}
	
	
	public ResourceAssignment assignResource(ResourceType t){
		String id = t.getId();
		ResourceAssignment r = resources.get(id);
		if(r != null)return r;//if already assigned => return 
		r = new ResourceAssignment(t);
		r.addTo(resourcesElement);
		resources.put(id,r);
		return r;
	}
	
	public void deassignResource(String id){
		ResourceAssignment r = resources.get(id);
		resources.remove(id);
		if(r != null)r.removeFrom(resourcesElement);
	}

}
