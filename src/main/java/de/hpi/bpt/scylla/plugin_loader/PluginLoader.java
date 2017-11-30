package de.hpi.bpt.scylla.plugin_loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.hpi.bpt.scylla.GUI.CheckBoxList.StateObserver;
import de.hpi.bpt.scylla.logger.DebugLogger;
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
	 * Loads all plugins from all packages from META-INF/plugins/plugins_list,
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

			ArrayList<String> packages = new ArrayList<String>();
			
			String line = null;
			while ((line = br.readLine()) != null) {
				packages.add(line);
			}
			
			br.close();
			fr.close();
			
			//Finding superclasses of plugins and sorting by them
			extensions = new HashMap<Class<?>,ArrayList<PluginWrapper>>();
			
			for(String pack : packages){
				try{
					String pack_slash  = pack.replace('.','/')+"/"; 
					URL packURL = PluginLoader.class.getClassLoader().getResource(pack_slash);
					if(packURL == null){
						DebugLogger.error("Error loading plugins: Could not find package "+pack);
						continue;
					}
					String[] classPaths = 
							packURL.getProtocol().equals("jar") ?
							getJarClassPaths(packURL, pack_slash) :
							getFilePaths(packURL, pack);
					for(int i = 0; i < classPaths.length; i++){
						String c = classPaths[i];
						if(!c.endsWith(".class"))continue;
						c = c.substring(0,c.lastIndexOf(".class"));
						
						try{
							Class<?> plugin = Class.forName(c);
							if(plugin == null){
								try{throw new Exception(plugin+" not found");}catch(Exception e){e.printStackTrace();continue;}
							}
							Class<?> entry_point = plugin.getSuperclass();
							if(entry_point == null || !IPluggable.class.isAssignableFrom(entry_point)){
								//try{throw new Exception(plugin+" is not a valid extension of an entry point");}catch(Exception e){e.printStackTrace();continue;}
							}else{
								if(!extensions.containsKey(entry_point))extensions.put(entry_point, new ArrayList<PluginWrapper>());
								if(!extensions.get(entry_point).contains(plugin))extensions.get(entry_point).add(new PluginWrapper(plugin,true));
							}
						}catch(ClassNotFoundException e){
							e.printStackTrace();
							continue;
						}
	
					}
				}catch (IllegalArgumentException e){
					e.printStackTrace();
					continue;
				}
			}
			
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Gets paths to all classes inside given package inside jar file
	 * @param packURL URL to package
	 * @param packname name of package in form superpackage/package/subpackage/
	 * @return String array of file path in form packname.filename.fileending
	 */
	private String[] getJarClassPaths(URL packURL, String pack_slash){
		String[] paths = null;
			JarURLConnection connection;
			try {
				connection = (JarURLConnection) packURL.openConnection();
				JarFile file = connection.getJarFile();
				Enumeration<JarEntry> en = file.entries();
				ArrayList<String> filePaths = new ArrayList<String>();
				while (en.hasMoreElements()) {
					JarEntry entry = en.nextElement();
					String c = entry.toString();
					if(!c.startsWith(pack_slash) || entry.isDirectory() || !c.endsWith(".class"))continue;
					c = c.replace("/",".");
					filePaths.add(c);
				}
				paths = new String[filePaths.size()];
				return filePaths.toArray(paths);
			} catch (IOException e) {
				e.printStackTrace();
			}
		return paths;
	}
	
	/**
	 * Gets paths to all filenames inside given package 
	 * @param packURL URL to package
	 * @param packname name of package in form superpackage.package.subpackage
	 * @return String array of file path in form packname.filename.fileending
	 */
	private String[] getFilePaths(URL packURL, String packname){
		String[] paths = null;
		try {
			File[] files = new File(packURL.toURI()).listFiles();
			paths = new String[files.length];
			for(int i = 0; i < files.length; i++){
				paths[i] = packname+"."+files[i].getName();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return paths;
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
	 * Will load plugins if not loaded!
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



