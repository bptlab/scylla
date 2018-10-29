package de.hpi.bpt.scylla.plugin.batch;

import java.util.Arrays;

/**
 * @project scylla
 * @author bjoern on 26.03.18
 */

// This class holds the execution types a batch cluster can have
public enum BatchClusterExecutionType {
    PARALLEL("parallel"), SEQUENTIAL_TASKBASED("sequential-taskbased"), SEQUENTIAL_CASEBASED("sequential-casebased");
	public final String elementName;
    private BatchClusterExecutionType(String elementName) {
    	this.elementName = elementName;
    }
    
    public static BatchClusterExecutionType ofElementName(String elementName) {
    	return Arrays.stream(BatchClusterExecutionType.values())
    			.filter(each -> each.elementName.equals(elementName))
    			.findAny().orElseThrow(IllegalArgumentException::new);
    }
}
