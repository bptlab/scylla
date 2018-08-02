package de.hpi.bpt.scylla;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
	

	
	public static void cleanupOutputs(String path) {
		File folder = new File(path);
		if(!folder.isDirectory() || !folder.exists())return;
		for (File f : folder.listFiles()) {
	    	if(f.isDirectory() && f.getName().startsWith("output_")) {
	    		deleteFolder(f);
	    	}
		}

	}
	
	public static void deleteFolder(File folder) {
		if(!folder.isDirectory() || !folder.exists())return;
		for (File f : folder.listFiles()) {
	    	f.delete();
		}
		folder.delete();
	}
	
	public static List<String[]> readCSV(File csvFile) {
		ArrayList<String[]> table = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(csvFile))){
			String header = br.readLine();
			if(header == null)return null;
			String line;
			while((line = br.readLine()) != null) {
				table.add(line.split(";"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return table;
	}

}
