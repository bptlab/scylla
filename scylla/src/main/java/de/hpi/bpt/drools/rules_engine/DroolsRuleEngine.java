package de.hpi.bpt.drools.rules_engine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.ObjectFilter;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import de.hpi.bpt.drools.model.bahncard.Input;
import de.hpi.bpt.drools.model.bahncard.DiscountValue_Output;
import de.hpi.bpt.scylla.plugin.dmn.RulesEngine;

public class DroolsRuleEngine implements RulesEngine {

	private KieSession kSession;
	
	@Override
	public void setup() {		
        KieServices ks = KieServices.Factory.get();
	    KieContainer kContainer = ks.getKieClasspathContainer();
	    kSession = kContainer.newKieSession("ksession-rules");
	}

	@Override
	public Map<String, String> evaluateRules(Map<String, Object> input) {
		
		// create input model
		Input inputModel = new Input();
		inputModel.setBahnCardPoints(new BigDecimal((long) input.get("BahnCard.Points")));
		inputModel.setBahnCardType((String)input.get("BahnCard.Type"));
		
		// insert input
		kSession.insert(inputModel);
		
		// evaluate rules
    	kSession.fireAllRules();

    	// get output
    	String discountValue = ((DiscountValue_Output)kSession.getObjects(new ObjectFilter() {
			public boolean accept(Object object) {
				return object.getClass().equals( DiscountValue_Output.class );
			}
		}).iterator().next()).getDiscountValue();
		
    	Map<String, String> output = new HashMap<String, String>();
		output.put("discountvalue", discountValue);
		
		return output;
	}
}
