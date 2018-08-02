package de.hpi.bpt.scylla.plugin_loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.GUI.CheckBoxList.StateObserver;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.plugin_type.IPluggable;


/**
 * @author Leon Bein
 */
@SuppressWarnings("rawtypes")
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

			//Finding superclasses of plugins and sorting by them
			extensions = new HashMap<Class<?>,ArrayList<PluginWrapper>>();
			
			for(String pack : getStandardPluginPackages()){
				try{
					String pack_slash  = pack.replace('.','/')+"/"; 
					Enumeration<URL> packURLs = PluginLoader.class.getClassLoader().getResources(pack_slash);
					if(!packURLs.hasMoreElements()){
						DebugLogger.error("Error loading plugins: Could not find package "+pack);
						continue;
					}
					for(URL packURL : Collections.list(packURLs)) {
						ArrayList<String> classPaths = 
								packURL.getProtocol().equals("jar") ?
									getJarClassPaths(packURL, pack_slash) :
									getFilePaths(packURL, pack);
						searchClassesForPlugins(getClassesIn(classPaths));
					}
				}catch (IllegalArgumentException e){
					e.printStackTrace();
					continue;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			searchClassesForPlugins(new ExternalJarLoader().loadClasses());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
	}
	
	private List<String> getStandardPluginPackages(){
		try(InputStreamReader fr = new InputStreamReader(PluginLoader.class.getResourceAsStream("/META-INF/plugins/plugins_list"))){
			try(BufferedReader br = new BufferedReader(fr)){
				ArrayList<String> packages = new ArrayList<String>();
				
				String line = null;
				while ((line = br.readLine()) != null) {
					packages.add(line);
				}
				return packages;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets paths to all classes inside given package inside jar file
	 * @param packURL URL to package
	 * @param packname name of package in form superpackage/package/subpackage/
	 * @return String array of file path in form packname.filename.fileending
	 */
	private ArrayList<String> getJarClassPaths(URL packURL, String pack_slash){
		ArrayList<String> filePaths = new ArrayList<String>();
			JarURLConnection connection;
			try {
				connection = (JarURLConnection) packURL.openConnection();
				JarFile file = connection.getJarFile();
				Enumeration<JarEntry> en = file.entries();
				while (en.hasMoreElements()) {
					JarEntry entry = en.nextElement();
					String c = entry.toString();
					if(!c.startsWith(pack_slash) || entry.isDirectory() || !c.endsWith(".class"))continue;
					c = c.replace("/",".");
					filePaths.add(c);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		return filePaths;
	}
	
	/**
	 * Gets paths to all filenames inside given package 
	 * @param packURL URL to package
	 * @param packname name of package in form superpackage.package.subpackage
	 * @return String array of file path in form packname.filename.fileending
	 */
	private ArrayList<String> getFilePaths(URL packURL, String packname){
		ArrayList<String> paths = new ArrayList<String>();
		try {
			File[] files = new File(packURL.toURI()).listFiles();
			for(int i = 0; i < files.length; i++){
				paths.add(packname+"."+files[i].getName());
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return paths;
	}
	
	private ArrayList<Class> getClassesIn(ArrayList<String> classPaths) {
		return ((ArrayList<Class>)classPaths.stream()
			.map(classPath -> {
				assert classPath.endsWith(".class");
				String className = classPath.substring(0, classPath.lastIndexOf(".class"));
				try {
					Class clazz = Class.forName(className);
					if (clazz == null) {throw new ClassNotFoundException(clazz + " not found");}
					return clazz;
				} catch(ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				}
			})
			.collect(Collectors.toCollection(ArrayList::new))
		);
	}
	
	private void searchClassesForPlugins(List<Class> classes) {
		for(Class c : classes) {
			if(isPluginClass(c))loadClass(c);
		}
	}
	
	private void loadClass(Class plugin) {
		Class<?> entry_point = plugin.getSuperclass();
		if (!extensions.containsKey(entry_point))
			extensions.put(entry_point, new ArrayList<PluginWrapper>());
		if (!extensions.get(entry_point).contains(plugin))
			extensions.get(entry_point).add(new PluginWrapper(plugin, true));
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
	
	public static boolean isPluginClass(Class<?> clazz) {
		Class<?> entry_point = clazz.getSuperclass();
		return entry_point != null && IPluggable.class.isAssignableFrom(entry_point);
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



