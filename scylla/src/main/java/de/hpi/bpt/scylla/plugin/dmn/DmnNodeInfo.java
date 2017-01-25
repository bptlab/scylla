package de.hpi.bpt.scylla.plugin.dmn;

import java.util.Map;

public class DmnNodeInfo {

    private String processScopeNodeId;
    private String source;
	private Map<String, Object> input;
	private Map<String, String> output;
	private String taskName;

    public DmnNodeInfo(String processScopeNodeId, String source, Map<String,Object> input, Map<String,String> output, String taskName ) { 
        this.processScopeNodeId = processScopeNodeId;
        this.source = source;
        this.input = input;
        this.output = output;
        this.taskName = taskName;
    }

    public String getProcessScopeNodeId() {
        return processScopeNodeId;
    }

    public String getSource() {
        return source;
    }

	public Map<String, Object> getInput() {
		return input;
	}

	public Map<String, String> getOutput() {
		return output;
	}
	
	public String getName() {
		return taskName;
	}
}
