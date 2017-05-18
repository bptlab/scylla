package de.hpi.bpt.scylla.plugin_loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import de.hpi.bpt.scylla.plugin_type.IPluggable;

//TODO delete this class, only for dev purpose
public class Crawler {
	
	public static void main(String[] args){
		
		
		try {
			File[] files = new File(Crawler.class.getClassLoader().getResource("META-INF/services").toURI()).listFiles();
			for(int i = 0; i < files.length; i++){
				System.out.println(files[i].getName());
			}
			ArrayList<String> plugins = new ArrayList<String>();
			for(int i = 0; i < files.length; i++){
				 FileReader fileReader = new FileReader(files[i]);

				 BufferedReader br = new BufferedReader(fileReader);

				 String line = null;
				 while ((line = br.readLine()) != null) {
					 plugins.add(line);

				 }
			}
			System.out.println("=================================================");
			 	
			for(String s : plugins){
				System.out.println(s);
			}
			
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
