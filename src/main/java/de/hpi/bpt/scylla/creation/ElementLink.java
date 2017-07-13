package de.hpi.bpt.scylla.creation;

import org.jdom2.Element;
import org.jdom2.Namespace;

public abstract class ElementLink {
	/**Standard Scylla namespace*/
	protected static final Namespace stdNsp = Namespace.getNamespace("bsim","http://bsim.hpi.uni-potsdam.de/scylla/simModel");

	
	protected Namespace nsp;
	protected Element el;
	
	/**
	 * Linking constructor, links Object with existing Element
	 * @param toLink
	 */
	public ElementLink(Element toLink){
		el = toLink;
		nsp = el.getNamespace();
	}
	
}
