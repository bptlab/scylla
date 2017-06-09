package de.hpi.bpt.scylla.plugin_loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.hpi.bpt.scylla.plugin_type.IPluggable;

//TODO delete this class, only for dev purpose
public class Crawler {
	
	public static void main(String[] args){
		String p = "org.jdom2.adapters";
		String s = p.replace('.','/')+"/";
		
		try {
			URL url = Crawler.class.getClassLoader().getResource(s);
			System.out.println(url.getPath());

			JarURLConnection connection = (JarURLConnection) url.openConnection();
			JarFile file = connection.getJarFile();
			String jarPath = file.getName();
			System.out.println(jarPath);
			ArrayList<Class> classes = new ArrayList<Class>();
			
			Enumeration<JarEntry> en = file.entries();
			while (en.hasMoreElements()) {
				JarEntry entry = en.nextElement();
				String c = entry.toString();
				if(!c.startsWith(s) || entry.isDirectory())continue;
				c = c.replace("/",".");
				System.out.println(c);
				//classes.add(Class.forName(c));
			}
			
			for(int i = 0; i < classes.size(); i++){
				System.out.println("Class "+classes.get(i).getName());
			}
			
//			ArrayList<String> plugins = new ArrayList<String>();
//			for(int i = 0; i < files.length; i++){
//				 FileReader fileReader = new FileReader(files[i]);
//
//				 BufferedReader br = new BufferedReader(fileReader);
//
//				 String line = null;
//				 while ((line = br.readLine()) != null) {
//					 plugins.add(line);
//
//				 }
//			}
//			System.out.println("=================================================");
//			 	
//			for(String s : plugins){
//				System.out.println(s);
//			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}