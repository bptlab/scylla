package de.hpi.bpt.drools.model.bahncard;

import java.math.BigDecimal;

public class Input {
	
	private String bahnCardtype;
	private BigDecimal bahnCardpoints;
	
	public String getBahnCardType() {
		return bahnCardtype;
	}
	
	public void setBahnCardType(String bahnCardtype){
		this.bahnCardtype = bahnCardtype;
	}
	
	public BigDecimal getBahnCardPoints() {
		return bahnCardpoints;
	}
	
	public void setBahnCardPoints(BigDecimal bahnCardpoints) {
		this.bahnCardpoints = bahnCardpoints;
	}
}
