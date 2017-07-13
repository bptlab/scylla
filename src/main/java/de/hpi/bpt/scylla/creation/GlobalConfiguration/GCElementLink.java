package de.hpi.bpt.scylla.creation.GlobalConfiguration;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;

/**
 * Package wrapper class to gain access to protected field el
 * @author Leon Bein
 *
 */
public abstract class GCElementLink extends ElementLink{

	public GCElementLink(Element toLink) {
		super(toLink);
	}
	
	protected Element getEl(){
		return el;
	}

}
