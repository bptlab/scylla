package de.hpi.bpt.scylla.plugin.eventArrivalRate;

import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.EventSchedulingPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.TimeSpan;

/**
 * Reschedules events that have a correspondent definition in the sc
 * @author Leon Bein
 *
 */
public class EventArrivalRateSchedulingPlugin extends EventSchedulingPluggable{

	@Override
	public String getName() {
		return EventArrivalRatePluginUtils.PLUGIN_NAME;
	}

	/**
	 * Gets a sample of the arrival rate distribution for this event, if existing,
	 * and schedules the event displaced by this value.
	 */
	@Override
	public boolean scheduleEvent(ScyllaEvent event, TimeSpan timeSpan) throws ScyllaRuntimeException {

        ProcessSimulationComponents pSimComponents = event.getDesmojObjects();

        Map<Integer, Object> arrivalRates = pSimComponents.getExtensionDistributions().get(getName());
        @SuppressWarnings("unchecked")
		SimpleEntry<NumericalDist<?>,TimeUnit> arrivalRate = (SimpleEntry<NumericalDist<?>, TimeUnit>) arrivalRates.get(event.getNodeId());
        if(arrivalRate != null) {
        	NumericalDist<?> distribution = arrivalRate.getKey();
        	TimeUnit timeUnit = arrivalRate.getValue();
			ProcessInstance processInstance = event.getProcessInstance();
			TimeSpan offset = new TimeSpan(distribution.sample().doubleValue(),timeUnit);
	        TimeSpan newTime = new TimeSpan(timeSpan.getTimeAsDouble()+offset.getTimeAsDouble());
	        event.schedule(processInstance, newTime);
	    	return false;
	    }
		
		return true;
	}

}
