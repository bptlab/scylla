package de.hpi.bpt.scylla.plugin.dataobject;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.configuration.distribution.DistributionWrapper;
import de.hpi.bpt.scylla.model.configuration.distribution.EmpiricalDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.EmpiricalStringDistribution;
import desmoj.core.dist.NumericalDist;

public class DataDistributionWrapper extends DistributionWrapper {

	/*private double min = -Double.MAX_VALUE;
	private double max = Double.MAX_VALUE;*/
	private NumericalDist<?> desmojDistribution;
	private DataDistributionType type;
	
	/*double getMin() {
		return this.min;
	}
	
	double getMax() {
		return this.max;
	}*/
	
	public DataDistributionWrapper(DataDistributionType type) {
		this.type = type;
	}
	
	public void setDesmojDistribution(NumericalDist<?> desmojDistribution) {
		this.desmojDistribution = desmojDistribution;
	}
	
	/*public void setMin(double min) {
		this.min = min;
	}
	
	public void setMax(double max) {
		this.max = max;
	}*/
	
	public Object getSample() throws ScyllaRuntimeException, ScyllaValidationException {
		if(desmojDistribution == null) {
			throw new ScyllaRuntimeException("desmojDistribution is not set.");
		}
		
		double value;
		
		
		/*do{
			value = desmojDistribution.sample().doubleValue();
		} while(min > value || value > max);*/
		
		// generate data in the given range with the given distribution, project it
		
		//value = min + (max-min) * ((desmojDistribution.sample().doubleValue() - Double.MAX_VALUE)/(Double.MAX_VALUE - Double.MAX_VALUE));
		
		value = desmojDistribution.sample().doubleValue();
		
		// handle LONG samples
		
		if( type == DataDistributionType.LONG ) {
			return Math.round(value);
		} 
		// handle STRING samples
		else if ( type == DataDistributionType.STRING ) {
			if(!(distribution instanceof EmpiricalStringDistribution)) {
				throw new ScyllaValidationException("Distribution is not an empirical string distribution, but the distribution type is String.");
			}
			
			EmpiricalStringDistribution es = (EmpiricalStringDistribution) distribution;
			return es.getNames().get(value);
		}
		// handle BOOLEAN samples
		else if( type == DataDistributionType.BOOLEAN ) { 
			if(!(distribution instanceof EmpiricalDistribution)) {
				throw new ScyllaValidationException("Distribution is not an empirical distribution, but the distribution type is Boolean.");
			}
			EmpiricalDistribution es = (EmpiricalDistribution) distribution;
			if(es.getEntries().size() != 2 
					|| !es.getEntries().containsKey(1.0)
					|| !es.getEntries().containsKey(0.0)) {
				throw new ScyllaValidationException("Distribution does not match the requirements for Boolean distribution type." );
			}
			return (value == 1.0);
		} 
		// handle default DOUBLE samples
		else {
			return value;
		}
	}
	
}
