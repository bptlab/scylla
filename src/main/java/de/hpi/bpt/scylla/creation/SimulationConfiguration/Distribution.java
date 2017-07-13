package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.Arrays;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;

public class Distribution extends ElementLink{
	
	
	private enum AttributeType{DOUBLE,INT,ENTRYSET}
	//TODO empirical distribution
	public enum DistributionType{
		binomial(		"binomial",			"Binomial", 		new String[]{"probability","amount"}, 	new AttributeType[]{AttributeType.DOUBLE,AttributeType.INT}),
		constant(		"constant", 		"Constant", 		new String[]{"constantValue"}, 			new AttributeType[]{AttributeType.DOUBLE}),
		empirical(		"empirical",		"Empirical", 		new String[]{"entry"}, 					new AttributeType[]{AttributeType.ENTRYSET}),
		empiricalString("empiricalString", 	"Empirical String", new String[]{"entry"}, 					new AttributeType[]{AttributeType.ENTRYSET}),
		erlang(			"erlang", 			"Erlang", 			new String[]{"order","mean"}, 			new AttributeType[]{AttributeType.INT,AttributeType.DOUBLE}),
		exponential(	"exponential", 		"Exponential", 		new String[]{"mean"}, 					new AttributeType[]{AttributeType.DOUBLE}),
		triangular(		"triangular", 		"Triangluar", 		new String[]{"lower","upper","peek"}, 	new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE,AttributeType.DOUBLE}),
		normal(			"normal", 			"Normal", 			new String[]{"mean","standardDeviation"}, new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE}),
		poisson(		"poisson", 			"Poisson", 			new String[]{"mean"}, 					new AttributeType[]{AttributeType.DOUBLE}),
		uniform(		"uniform", 			"Uniform", 			new String[]{"lower","upper"}, 			new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE})
		;
		public final String id;
		public final String displayName;
		public final String[] attributes;
		public final AttributeType[] types;
		private DistributionType(String i, String n, String[] a, AttributeType[] t){
			if(a.length != t.length)throw new ExceptionInInitializerError("Quasi compile error, no valid distribution type definition");
			id = i+"Distribution";
			displayName = n+" Distribution";
			attributes = a;
			types = t;
		}
	}
	
	private DistributionType type;
	
	/**
	 * Link constructor
	 * @param toLink
	 */
	private Distribution(Element toLink) {
		super(toLink);
		try{
			type = DistributionType.valueOf(toLink.getName().split("Distribution")[0]);
		}catch(IllegalArgumentException e){
			throw new ExceptionInInitializerError("Error at creating distribution wrapper - "+toLink.getName()+" is not a valid distribution id");
		}
	}
	
	public Distribution(DistributionType t){
		super(new Element(t.id,stdNsp));
		type = t;
		for(String attribute : type.attributes){
			el.addContent(new Element(attribute,nsp));
		}
	}
	
	public void setAttribute(int index, Object value){
		if(index < type.attributes.length)
		el.getChild(type.attributes[index], nsp).setText(value.toString());
	}
	
	
	public void setAttribute(String id, Object value){
		if(Arrays.asList(type.attributes).contains(id))
			el.getChild(id, nsp).setText(value.toString());
	}
	
	public String getAttribute(int index){
		if(index < type.attributes.length)return el.getChildText(type.attributes[index], nsp);
		return null;
		
	}
	
	public String getAttribute(String id){
		if(Arrays.asList(type.attributes).contains(id))return el.getChildText(id, nsp);
		return null;
	}
	
}
