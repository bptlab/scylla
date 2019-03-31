package de.hpi.bpt.scylla.plugin_loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.GUI.CheckBoxList.StateObserver;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.plugin_loader.DependencyGraph.CycleException;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import de.hpi.bpt.scylla.plugin_type.simulation.event.GatewayEventPluggable;


/**
 * @author Leon Bein
 */
@SuppressWarnings("rawtypes")
public class PluginLoader {
	

	
	/**Saves all entry point classes and their plugins*/
	private HashMap<Class<?>, List<PluginWrapper>> extensions;
	
	/**Caches created plugin objects*/
	private Map<Class<? extends IPluggable>,IPluggable> cachedPluginObjects = new HashMap<>();
	
	/**Default plugin loader*/
	private static PluginLoader defaultPluginLoader;
	
	
	
	public class PluginWrapper<T extends IPluggable> implements EventListener,StateObserver{
		private Class<T> plugin;
		private List<TemporalDependent> temporalDependencies;
		private boolean chosen;
		
		private PluginWrapper(Class<T> p, boolean b){
			plugin = p;
			chosen = b;
			parseTemporalDependencies();
		}
		
		private void parseTemporalDependencies() {
			temporalDependencies = Arrays.asList(plugin.getAnnotationsByType(TemporalDependent.class));
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
		
		public List<TemporalDependent> getTemporalDependents() {return temporalDependencies;}
		
		private T getInstance(){
			try {
				return PluginLoader.this.getInstance(plugin);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				System.err.println("Failed to instantiate plugin "+plugin.getSimpleName());
				e.printStackTrace();
				return null;
			}
		}
	}
	
	
	
	/**
	 * Loads all plugins from all packages from META-INF/plugins/plugins_list,
	 * gets the corresponding classes 
	 * and sorts them by their entry point superclasses
	 * Saves the data to {@link de.hpi.bpt.scylla.plugin_loader.PluginLoader.extensions}
	 */
	public void loadPlugins(){

		//Finding superclasses of plugins and sorting by them
		extensions = new HashMap<Class<?>,List<PluginWrapper>>();
		
		for(String pack : getStandardPluginPackages()){
			try{
				loadPackage(pack);
			}catch (IllegalArgumentException | IOException e){
				e.printStackTrace();
				continue;
			}
		}
		
		try {
			searchClassesForPlugins(new ExternalJarLoader().loadClasses());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
	}
	
	public void loadPackage(String pack) throws IOException {
		String pack_slash  = pack.replace('.','/')+"/"; 
		Enumeration<URL> packURLs = PluginLoader.class.getClassLoader().getResources(pack_slash);
		if(!packURLs.hasMoreElements()){
			DebugLogger.error("Error loading plugins: Could not find package "+pack);
			return;
		}
		for(URL packURL : Collections.list(packURLs)) {
			ArrayList<String> classPaths = 
					packURL.getProtocol().equals("jar") ?
						getJarClassPaths(packURL, pack_slash) :
						getFilePaths(packURL, pack);
			searchClassesForPlugins(getClassesIn(classPaths));
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
	
	@SuppressWarnings("unchecked")
	private void searchClassesForPlugins(List<Class> classes) {
		for(Class c : classes) {
			if(isPluginClass(c))loadClass(c);
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	private <T extends IPluggable> void loadClass(Class<T> plugin) {
		Class<?> entry_point = plugin.getSuperclass();
		if (!extensions.containsKey(entry_point))
			extensions.put(entry_point, new ArrayList<PluginWrapper>());
		if (!extensions.get(entry_point).contains(plugin))
			extensions.get(entry_point).add(new PluginWrapper<T>(plugin, true));
	}
	

	
	/**
	 * Prints out all available plugins sorted by entry point
	 */
	public void printPlugins(){
		if(extensions == null)return;
		for(Map.Entry<Class<?>, List<PluginWrapper>> entry_point : extensions.entrySet()){
			System.out.println(entry_point.getKey().getName());
			List<PluginWrapper> plugins = entry_point.getValue();
			for(PluginWrapper plugin : plugins){
				System.out.println("\t "+plugin.toString());
			}
		}
	}
	
	public void prepareForSimulation() throws CycleException {
		resolveTemporalDependencies();
	}
	
	public void resolveTemporalDependencies () throws CycleException {
		//TODO ignore non selected plugin
		for(Entry<Class<?>, List<PluginWrapper>> entryPoint : getExtensions().entrySet()) {
			if(!entryPoint.getKey().equals(GatewayEventPluggable.class))continue;//TODO why only gatewayevent?
			try {
				getExtensions().put(entryPoint.getKey(), resolveTemporalDependencies(entryPoint.getValue()));
			} catch(CycleException e) {
				throw (CycleException)new CycleException(
						"Error at plugin loader: Could not resolve temporal dependencies of entrypoint "+entryPoint.getKey().getSimpleName()
				).initCause(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<PluginWrapper> resolveTemporalDependencies(List<PluginWrapper> plugins) throws CycleException {
		DependencyGraph<PluginWrapper> graph = new DependencyGraph<>(plugins);
		Map<Class, PluginWrapper> classMap = new HashMap<>();
		for(PluginWrapper plugin : plugins) {
			classMap.put(plugin.plugin, plugin);
		}
		for(PluginWrapper plugin : plugins) {
			for(TemporalDependent dependency : (List<TemporalDependent>)plugin.getTemporalDependents()) {
				PluginWrapper dependencyPlugin = classMap.get(dependency.value());
				if(dependencyPlugin == null)continue;//
				switch(dependency.execute()) {
				case AFTER :  graph.createEdge(dependencyPlugin, plugin); break;
				case BEFORE : graph.createEdge(plugin, dependencyPlugin); break;
				}
			}
		}
		
		return graph.resolve();
	}

	/**
	 * @return All loaded Plugins, sorted by their entry point
	 */
	public Map<Class<?>, List<PluginWrapper>> getExtensions() {		
		return extensions;
	}
	
	/**
	 * Returns all loaded plugins that extend a given entrypoint or one of its sub-entrypoints)
	 * @param entrypoint The entrypoint to be extended
	 * @return ArrayList of loaded plugins
	 */
	@SuppressWarnings("unchecked")
	public <S extends IPluggable> List<S> getPlugins(Class<S> entrypoint) {
		return (List<S>) extensions.entrySet().stream()
			.filter(each -> entrypoint.isAssignableFrom(each.getKey()))
			.flatMap(each -> each.getValue().stream())
			.filter(PluginWrapper::getState)
			.map(PluginWrapper::getInstance)
			.collect(Collectors.toList());
	}
	
	private <T extends IPluggable> T getInstance(Class<T> entryPoint) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		T inst = getCachedInstance(entryPoint);
		if(inst == null) {
			@SuppressWarnings("unchecked")
			Class<? extends IPluggable> enclosingClass = (Class<? extends IPluggable>) entryPoint.getEnclosingClass();
			if(enclosingClass == null || Modifier.isStatic(entryPoint.getModifiers())){
				inst = (T)(entryPoint.newInstance());
			} else {
				inst = (T)(entryPoint.getDeclaredConstructor(enclosingClass).newInstance(getInstance(enclosingClass)));
			}
			cacheInstance(entryPoint, inst);
		}
		return inst;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends IPluggable> T getCachedInstance(Class<T> entryPoint) {
		return (T) cachedPluginObjects.get(entryPoint);
	}
	
	private <T extends IPluggable> void cacheInstance(Class<T> entryPoint, T instance) {
		cachedPluginObjects.put(entryPoint, instance);
	}
	
	private void flushCache() {
		cachedPluginObjects.clear();
	}
	
	public static void flushDefault() {
		getDefaultPluginLoader().flushCache();
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



