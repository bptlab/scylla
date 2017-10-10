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
	
	/**
	 * Adds this objects wrapped element to a given element
	 * @param e : The element to be added to
	 */
	public void addTo(Element e){
		e.addContent(el);
	}
	
	/**
	 * Removes this objects wrapped element from a given element
	 * @param e : The element to be removed from
	 */
	public void removeFrom(Element e){
		e.removeContent(el);
	}
	
	/**
	 * Sets an attribute with given name to a value object, that is converted by its toString() method
	 * @param name attribute name
	 * @param value attribute value
	 */
	protected void setAttribute(String name, Object value){
		el.setAttribute(name, value.toString());
	}
	
}
