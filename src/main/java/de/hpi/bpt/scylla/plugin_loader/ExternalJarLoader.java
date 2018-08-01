package de.hpi.bpt.scylla.plugin_loader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public class ExternalJarLoader {
	
	private List<Path> pluginPaths;
	private List<String> classNames;
	private List<Class> classes;
	
	public List<Class> loadClasses() throws IOException {
		getPluginPaths();
		//for(Object s : pluginPaths)System.out.println("Jar file: "+s);
		collectClasses();
		//for(String e : classes)System.out.println("Class: "+e);
		openClassLoader();
		
		return classes;
	}
	
	private List<Path> getPluginPaths() throws IOException {
		try (Stream<Path> paths = Files.walk(Paths.get("plugins"))) {
		    pluginPaths = paths
		        .filter(Files::isRegularFile)
		        .filter(ExternalJarLoader::hasJarEnding)
		        .collect(Collectors.toList());
		}
		return pluginPaths;
	}
	
	private void collectClasses() throws IOException {
		classNames = new ArrayList<String>();
		for(Path path : pluginPaths) {
			try(JarFile jarFile = new JarFile(path.toString(), true)){
				Enumeration<JarEntry> entries = jarFile.entries();
		        while (entries.hasMoreElements()) {
		        	JarEntry next = entries.nextElement();
		        	if(next.getName().endsWith(".class"))classNames.add(className(next));
		        }
			}
		}
	}
	
	private void openClassLoader() throws IOException {
		URL[] jars = pluginPaths.stream()
				.map(ExternalJarLoader::toURL)
				.filter(Objects::nonNull)
				.toArray(URL[]::new);
		try(URLClassLoader classLoader = new URLClassLoader(jars, PluginLoader.class.getClassLoader())){
			classes = new ArrayList<Class>();
			for(String clazz : classNames) {
				classLoader.loadClass(clazz);
				Class<?> c = Class.forName(clazz,true,classLoader);
				classes.add(c);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		};
	}
	
	private static String className(JarEntry e) {
		String name = e.getName();
		assert name.endsWith(".class");
		return name
				.substring(0, name.lastIndexOf("."))
				.replace("/",".");
	}
	
	private static boolean hasJarEnding(Path path) {
		return path.toString().endsWith(".jar");
	}
	
	private static URL toURL(Path path) {
		try {
			return path.toUri().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
