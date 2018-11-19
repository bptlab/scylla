package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;

import de.hpi.bpt.scylla.plugin.dataobject.DataObjectField;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;

/**
 * 
 * @author was not Leon Bein
 *
 */
public abstract class ThresholdRule implements ActivationRule{

    private int threshold;

    public static ThresholdRule create(int threshold, Duration timeout) {
        return new TimeOutThresholdRule(threshold, timeout);
    }

    public static ThresholdRule create(int threshold, String dueDate) {
    	return new DueDateThresholdRule(threshold, dueDate);
    }
    
    private ThresholdRule(int threshold) {
        this.threshold = threshold;
    }

    public int getThreshold(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {
        return threshold;
    }
    
    private static class TimeOutThresholdRule extends ThresholdRule {

    	private Duration timeOut;

		public TimeOutThresholdRule(int threshold, Duration timeout) {
			super(threshold);
	        this.timeOut = timeout;
		}
		
		public Duration getTimeOut(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {
			return this.timeOut;
		}
    	
    }
    
    /**
     * 
     * @author Leon Bein
     * @deprecated see TODOs in {@link DueDateThresholdRule#getDurationForCurrentInstance(TaskBeginEvent, ProcessInstance)}
     */
    private static class DueDateThresholdRule extends ThresholdRule {

        private String dueDate;

        public DueDateThresholdRule (int threshold, String dueDate) {
            super(threshold);
            this.dueDate = dueDate;
        }
		
		public Duration getTimeOut(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {
            return Duration.ofDays(getDurationForCurrentInstance(desmojEvent, processInstance));
		}
		
	    private long getDurationForCurrentInstance(TaskBeginEvent desmojEvent, ProcessInstance processInstance){
	        long numberOfDays = 0;

	        //***********
	        // get the value of the dataObject
	        //***********

	        //SimulationModel model = (SimulationModel) desmojEvent.getModel();

	        numberOfDays = (long) DataObjectField.getDataObjectValue(processInstance.getId(),dueDate);
	        //TODO make due date a real date and calculate remaining time until duedate
	        //TODO should the result be fixed at some point?

	        return numberOfDays;
	    }
    	
    }



}
