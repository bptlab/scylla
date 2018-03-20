package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;

import de.hpi.bpt.scylla.plugin.dataobject.DataObjectField;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;


public class ThresholdRule implements ActivationRule{

    private int threshold;
    private Duration timeOut;
    private String dueDate;
    
    public ThresholdRule (int threshold, Duration timeout) {
        this.threshold = threshold;
        this.timeOut = timeout;
        this.dueDate = null;
    }
    
    public ThresholdRule (int threshold, String dueDate) {
        this.threshold = threshold;
        this.timeOut = null;
        this.dueDate = dueDate;
    }

	public int getThreshold(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {
		return threshold;
	}

	public Duration getTimeOut(TaskBeginEvent desmojEvent, ProcessInstance processInstance){
		if (timeOut != null){
			return timeOut;
		}else{
			
			return Duration.ofDays(getDurationForCurrentInstance(desmojEvent, processInstance));
	}
		
	}
	
	private long getDurationForCurrentInstance(TaskBeginEvent desmojEvent, ProcessInstance processInstance){
		long numberOfDays = 0;
		
		//***********
		// get the value of the dataObject  
		//***********

        SimulationModel model = (SimulationModel) desmojEvent.getModel();

        numberOfDays = (long) DataObjectField.getDataObjectValue(processInstance.getId(),dueDate);

        System.out.println("Due Date for "+ desmojEvent+" in: "+ numberOfDays);

		return numberOfDays;
	}


}
