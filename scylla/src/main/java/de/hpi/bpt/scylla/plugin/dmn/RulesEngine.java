package de.hpi.bpt.scylla.plugin.dmn;

import java.util.Map;

public interface RulesEngine {

	public void setup();
	
	public Map<String, String> evaluateRules(Map<String, Object> input);
}
