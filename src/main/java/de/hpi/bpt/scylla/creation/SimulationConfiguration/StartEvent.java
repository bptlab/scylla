package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.concurrent.TimeUnit;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;

/**
 * Start event wrapper class for SCs
 * @author Leon Bein
 *
 */
public class StartEvent extends ElementLink{
	
	/** Arrival distribution*/
	private Distribution arrivalRateDistribution;
	/** Arrival rate element*/
	private Element arrivalRate;
	
	/**
	 * Link constructor, links to existing startevent element, parses distribution
	 * @param toLink : Element to link with
	 */
	public StartEvent(Element toLink) {
		super(toLink);
		arrivalRate = el.getChild("arrivalRate",nsp);
		arrivalRateDistribution = Distribution.create(arrivalRate.getChildren().get(0));
	}
	
	/**
	 * Constructor, creates a new object and element with given id, creates an empty arrivalrate element
	 * @param id
	 */
	public StartEvent(String id) {
		super(new Element("startEvent",stdNsp));
		setAttribute("id",id);
		arrivalRate = new Element("arrivalRate",nsp);
		el.addContent(arrivalRate);
		setArrivalTimeUnit(TimeUnit.SECONDS);		
	}
	
	/**
	 * Sets the arrival distribution; removes any existing distribution
	 * @param d : Wrapper for element to be set as distribution
	 */
	public void setArrivalRateDistribution(Distribution d){
		if(arrivalRateDistribution != null){
			arrivalRate.removeContent();
		}
		arrivalRateDistribution = d;
		d.addTo(arrivalRate);
	}
	
	/**
	 * @return wrapper object for arrival rate distribution, if already specified
	 */
	public Distribution getArrivalRateDistribution(){
		return arrivalRateDistribution;
	}
	
	/**
	 * Sets the arrival time unit
	 * @param t : Time unit to be set
	 */
	public void setArrivalTimeUnit(TimeUnit t){
		arrivalRate.setAttribute("timeUnit",t.toString());
	}
	
	/**
	 * @return String value of the specified arrival time unit
	 */
	public String getArrivalTimeUnit(){
		return arrivalRate.getAttributeValue("timeUnit");
	}
	
	

}
