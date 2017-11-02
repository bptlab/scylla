package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.Arrays;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;

public class Distribution extends ElementLink{
	
	
	public enum AttributeType{DOUBLE,INT,ENTRYSET}
	public enum DistributionType{
		binomial(		"binomial",					"Binomial", 		new String[]{"probability","amount"}, 	new AttributeType[]{AttributeType.DOUBLE,AttributeType.INT}),
		constant(		"constant", 				"Constant", 		new String[]{"constantValue"}, 			new AttributeType[]{AttributeType.DOUBLE}),
		erlang(			"erlang", 					"Erlang", 			new String[]{"order","mean"}, 			new AttributeType[]{AttributeType.INT,AttributeType.DOUBLE}),
		exponential(	"exponential", 				"Exponential", 		new String[]{"mean"}, 					new AttributeType[]{AttributeType.DOUBLE}),
		triangular(		"triangular", 				"Triangluar", 		new String[]{"lower","peak","upper"}, 	new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE,AttributeType.DOUBLE}),
		normal(			"normal", 					"Normal", 			new String[]{"mean","standardDeviation"}, new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE}),
		poisson(		"poisson", 					"Poisson", 			new String[]{"mean"}, 					new AttributeType[]{AttributeType.DOUBLE}),
		uniform(		"uniform", 					"Uniform", 			new String[]{"lower","upper"}, 			new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE}),
		arbitraryFiniteProbability(		"arbitraryFiniteProbability", "Descrete", 		new String[]{"entry"}, 					new AttributeType[]{AttributeType.ENTRYSET}),
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
		
		@Override
		public String toString(){
			return displayName;
		}
	}
	
	protected final DistributionType type;
	
	
	public static Distribution create(Element toLink){
		try{
			DistributionType type = DistributionType.valueOf(toLink.getName().split("Distribution")[0]);
			switch(type){
			case arbitraryFiniteProbability : return new DiscreteDistribution(toLink);
			default : return new Distribution(toLink, type);
			}
		}catch(IllegalArgumentException e){
			throw new ExceptionInInitializerError("Error at creating distribution wrapper - "+toLink.getName()+" is not a valid distribution id");
		}
	}
	
	public static Distribution create(DistributionType type){
		switch(type){
		case arbitraryFiniteProbability : return new DiscreteDistribution();
		default : return new Distribution( type);
		}
	}
	
	/**
	 * Link constructor
	 * @param toLink
	 */
	protected Distribution(Element toLink,DistributionType t) {
		super(toLink);
		type = t;
	}
	
	protected Distribution(DistributionType t){
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
		if(index >= 0 && index < type.attributes.length)return el.getChildText(type.attributes[index], nsp);
		return null;
		
	}
	
	public String getAttribute(String id){
		if(Arrays.asList(type.attributes).contains(id))return el.getChildText(id, nsp);
		return null;
	}
	
	public DistributionType getType(){
		return type;
	}
	
	public static class DiscreteDistribution extends Distribution{

		private DiscreteDistribution(Element toLink) {
			super(toLink,DistributionType.arbitraryFiniteProbability);
		}
		
		private DiscreteDistribution() {
			super(DistributionType.arbitraryFiniteProbability);
		}
		
		public void addEntry(Object value, Double frequency){
			Element entry = new Element("entry",nsp);
			entry.setAttribute("value", value.toString());
			entry.setAttribute("frequency",frequency.toString());
			el.addContent(entry);
		}
		
		public void removeEntry(Object value){
			for(Element e : el.getChildren()){
				if(e.getAttributeValue("value").equals(value.toString())){
					el.removeContent(e);
					break;
				}
			}
		}
		
		public void removeEntry(int index){
			el.removeContent(index);
		}
		
		public int getEntrySize(){
			return el.getChildren().size();
		}
		
		public Object getValue(int index){
			return el.getChildren().get(index).getAttributeValue("value");
		}
		
		public double getFrequency(int index){
			return Double.parseDouble(el.getChildren().get(index).getAttributeValue("frequency"));
		}
		
		public void setValue(int index, Object value){
			el.getChildren().get(index).setAttribute("value",value.toString());
		}
		
		public void setFrequency(int index, double value){
			el.getChildren().get(index).setAttribute("frequency",value+"");
		}
		
	}
	
}
