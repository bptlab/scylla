package de.hpi.bpt.scylla.plugin_loader;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

//TODO remove
public class TestPlugin extends de.hpi.bpt.scylla.plugin_type.parser.EventOrderType{

	@Override
	public int compare(String resourceId, ScyllaEvent e1, ScyllaEvent e2) throws ScyllaRuntimeException {
		System.out.println("0000000000000000000Test Plugin0000000000000000");
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
