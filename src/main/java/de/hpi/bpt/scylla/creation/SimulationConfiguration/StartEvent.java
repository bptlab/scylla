package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.concurrent.TimeUnit;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;

public class StartEvent extends ElementLink{
	
	private Distribution arrivalRateDistribution;
	private Element arrivalRate;
	
	/**
	 * Link constructor
	 * @param toLink
	 */
	private StartEvent(Element toLink) {
		super(toLink);
	}
	
	public StartEvent(String id) {
		super(new Element("startEvent",stdNsp));
		setAttribute("id",id);
		arrivalRate = new Element("arrivalRate",nsp);
		el.addContent(arrivalRate);
		setArrivalTimeUnit(TimeUnit.SECONDS);		
	}
	
	public void setArrivalRateDistribution(Distribution d){
		if(arrivalRateDistribution != null){
			arrivalRate.removeChild("distribution", nsp);
		}
		arrivalRateDistribution = d;
		d.addTo(arrivalRate);
	}
	
	public Distribution getArrivalRateDistribution(){
		return arrivalRateDistribution;
	}
	
	public void setArrivalTimeUnit(TimeUnit t){
		arrivalRate.setAttribute("timeUnit",t.toString());
	}
	
	public String getArritvalTimeUnit(){
		return arrivalRate.getAttributeValue("timeUnit");
	}
	
	

}
