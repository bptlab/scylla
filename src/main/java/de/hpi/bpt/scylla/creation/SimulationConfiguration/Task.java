package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;

public class Task extends ElementLink{

	private Element duration;
	private Distribution durationDistribution;
	private Element resourcesElement;
	private Map<String,Resource> resources;
	
	/**
	 * Link constructor
	 * @param toLink
	 */
	private Task(Element toLink) {
		super(toLink);
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
		resources = new HashMap<String, Resource>();
	}


	public String getId(){return el.getAttributeValue("id");}
	public String getName(){return el.getAttributeValue("name");}
	

	private void setDurationTimeUnit(TimeUnit t) {
		duration.setAttribute("timeUnit",t.toString());
	}
	public String getDurationTimeUnit(){
		return duration.getAttributeValue("timeUnit");
	}
	
	public void setDurationDistribution(Distribution d){
		if(durationDistribution != null){
			duration.removeChild("distribution", nsp);
		}
		durationDistribution = d;
		d.addTo(duration);
	}
	
	public Distribution getDurationDistribution(){
		return durationDistribution;
	}
	
	public Resource getResource(String id){
		return resources.get(id);
	}
	
	
	public Resource assignResource(GlobalConfigurationCreator.ResourceType t){
		String id = t.getId();
		Resource r = resources.get(id);
		if(r != null)return r;//if already assigned => return 
		r = new Resource(id,t.getName());
		r.addTo(resourcesElement);
		resources.put(id,r);
		return r;
	}
	
	public void deassignResource(String id){
		resources.remove(id);
		Resource r = resources.get(id);
		if(r != null)r.removeFrom(resourcesElement);
	}

}
