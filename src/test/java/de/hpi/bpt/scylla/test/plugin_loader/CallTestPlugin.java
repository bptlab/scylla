package de.hpi.bpt.scylla.test.plugin_loader;

import java.io.IOException;
import java.util.Iterator;

import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_type.logger.OutputLoggerPluggable;
import de.hpi.bpt.scylla.simulation.SimulationModel;

public class CallTestPlugin extends OutputLoggerPluggable{
	
	static {
		try {
			PluginLoader.getDefaultPluginLoader().loadPackage(CallTestPlugin.class.getPackage().getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

		
	public boolean called;

	public String getName() {return getClass().getSimpleName();}
	
	public static CallTestPlugin getInstance() {
		Iterator<OutputLoggerPluggable> iterator = PluginLoader.dGetPlugins(OutputLoggerPluggable.class);
		String found = "";
		while(iterator.hasNext()) {
			OutputLoggerPluggable next = iterator.next();
			found += next.getClass().getSimpleName()+"\n";
			if(next instanceof CallTestPlugin)return (CallTestPlugin) next;
		}
		throw new Error(CallTestPlugin.class.getSimpleName()+" instance not found, only\n"+found);
	}

	@Override
	public void writeToLog(SimulationModel model, String outputPathWithoutExtension) throws IOException {
		called = true;
	}
}
