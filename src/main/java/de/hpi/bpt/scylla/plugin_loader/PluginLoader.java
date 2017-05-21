package de.hpi.bpt.scylla.plugin_loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hpi.bpt.scylla.GUI.CheckBoxList.StateObserver;
import de.hpi.bpt.scylla.plugin_type.IPluggable;

/**
 * @author Leon Bein
 */
public class PluginLoader {
	
	/**Saves all entry point classes and their plugins*/
	private HashMap<Class<?>,ArrayList<PluginWrapper>> extensions;
	
	/**Default plugin loader*/
	private static PluginLoader defaultPluginLoader;
	
	
	public class PluginWrapper implements EventListener,StateObserver{
		private Class<?> plugin;
		private boolean chosen;
		private PluginWrapper(Class<?> p, boolean b){
			plugin = p;
			chosen = b;
		}
		@Override
		public String toString(){
			return plugin.getSimpleName();
		}
		
		/**
		 * @return true if:<br>
		 * - this == p <br>
		 * - this.plugin equals p.plugin<br>
		 * - this.plugin equals p
		 */
		@Override
		public boolean equals(Object p){
			return p instanceof PluginWrapper ? plugin.equals(((PluginWrapper)p).plugin) : 
				p instanceof Class<?> ? plugin.equals(p) : false;
		}

		@Override
		public void stateChanged(boolean b) {
			chosen = b;
			
		}
		@Override
		public boolean getState() {
			return chosen;
		}
		public Package getPackage(){
			return plugin.getPackage();
		}
	}
	
	
	
	/**
	 * Loads all plugins from META-INF/plugins/plugins_list,
	 * gets the corresponding classes 
	 * and sorts them by their entry point superclasses
	 * Saves the data to {@link de.hpi.bpt.scylla.plugin_loader.PluginLoader.extensions}
	 */
	public void loadPlugins(){
		
		try {
			//Collecting all plugin classpaths
			File plugins_list = new File(PluginLoader.class.getClassLoader().getResource("META-INF/plugins/plugins_list").toURI());

			FileReader fr = new FileReader(plugins_list);
			BufferedReader br = new BufferedReader(fr);

			ArrayList<String> plugin_classpaths = new ArrayList<String>();
			
			String line = null;
			while ((line = br.readLine()) != null) {
				plugin_classpaths.add(line);
			}
			
			br.close();
			fr.close();
			
			//Finding superclasses of plugins and sorting by them
			extensions = new HashMap<Class<?>,ArrayList<PluginWrapper>>();
			
			for(String s : plugin_classpaths){
				Class<?> plugin = Class.forName(s);
				if(plugin == null){
					try{throw new Exception(plugin+" not found");}catch(Exception e){e.printStackTrace();continue;}
				}
				Class<?> entry_point = plugin.getSuperclass();
				if(entry_point == null || !IPluggable.class.isAssignableFrom(entry_point)){
					try{throw new Exception(plugin+" is not a valid extension of an entry point");}catch(Exception e){e.printStackTrace();continue;}
				}
				if(!extensions.containsKey(entry_point))extensions.put(entry_point, new ArrayList<PluginWrapper>());
				if(!extensions.get(entry_point).contains(plugin))extensions.get(entry_point).add(new PluginWrapper(plugin,true));
			}
			
		} catch (URISyntaxException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	

	
	/**
	 * Prints out all available plugins sorted by entry point
	 */
	public void printPlugins(){
		if(extensions == null)return;
		for(Map.Entry<Class<?>,ArrayList<PluginWrapper>> entry_point : extensions.entrySet()){
			System.out.println(entry_point.getKey().getName());
			ArrayList<PluginWrapper> plugins = entry_point.getValue();
			for(PluginWrapper plugin : plugins){
				System.out.println("\t "+plugin.toString());
			}
		}
	}

	/**
	 * @return All loaded Plugins, sorted by their entry point
	 */
	public Map<Class<?>, ArrayList<PluginWrapper>> getExtensions() {		
		return extensions;
	}
	
	/**
	 * Returns all loaded plugins that extends a given entrypoint or one of its sub-entrypoints
	 * @param entrypoint The entrpoint to be extended
	 * @return ArrayList of loaded plugins
	 */
	public <S extends IPluggable> ArrayList<S> getPlugins(Class<S> entrypoint){
		ArrayList<S> l = new ArrayList<S>();
		
		for(Map.Entry<Class<?>,ArrayList<PluginWrapper>> extension : extensions.entrySet()){
			Class<?> savedEntrypoint = extension.getKey();
			if(entrypoint.isAssignableFrom(savedEntrypoint)){
				ArrayList<PluginWrapper> plugins = extension.getValue();
				for(int i = 0; i < plugins.size(); i++){
					PluginWrapper pl = plugins.get(i);
					if(pl.getState())
						try {
							@SuppressWarnings("unchecked")
							S inst = (S)(pl.plugin.newInstance());
							if(inst != null){
								l.add(inst);
								//System.out.println("Added Plugin "+pl.plugin.getCanonicalName());
							}
							//System.out.println("Entrypoint: "+savedEntrypoint+" \t Loading plugin "+pl.plugin.getName());
						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
							continue;
						}
				}
			}
		}
		
		return l;
	}
	
	
	/**
	 * @return Default Plugin_loader Object
	 */
	public static PluginLoader getDefaultPluginLoader(){
		if(defaultPluginLoader == null){
			defaultPluginLoader = new PluginLoader();
			defaultPluginLoader.loadPlugins();
		}
		return defaultPluginLoader;
	}
	
	/**
	 * Shortcut for getDefaultPluginLoader().getPlugins(entrypoint).iterator()
	 * @param entrypoint
	 * @return Iterator of all loaded plugins
	 */
	public static <S extends IPluggable> Iterator<S> dGetPlugins(Class<S> entrypoint){
		return getDefaultPluginLoader().getPlugins(entrypoint).iterator();
	}

}



