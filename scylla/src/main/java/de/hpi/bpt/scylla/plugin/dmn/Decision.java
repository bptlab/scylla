package de.hpi.bpt.scylla.plugin.dmn;

public class Decision {
	
	private String name;
	private String input;
	private String output;
	
	public Decision(String name, String input, String output){
		this.name = name;
		this.input = input;
		this.output = output;
	}
	
	public String getName() {
		return name;
	}
	
	public String getInput() {
		return input;
	}
	
	public String getOutput() {
		return output;
	}
}
