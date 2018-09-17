package de.hpi.bpt.scylla;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUtils {
	
	public static final String RESOURCEFOLDER = "src\\test\\resources\\";
	
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
				table.add(line.split(";",-1));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return table;
	}
	
	//TODO Probably move to special batch-test-utils
	public static Map<String, List<String[]>> groupByCluster(List<String[]> table){
		/*Map<String, List<String[]>> clusters = new HashMap<String, List<String[]>>();
		for(String[] row : table) {
			String batchNumber = row[6];
			if(!clusters.containsKey(batchNumber))clusters.put(batchNumber, new ArrayList<String[]>());
			clusters.get(batchNumber).add(row);
		}
		return clusters;*/
		return groupBy(table,6);
	}
	
	public static Map<String, List<String[]>> groupBy(List<String[]> table, int column){
		Map<String, List<String[]>> groups = new HashMap<String, List<String[]>>();
		for(String[] row : table) {
			String key = row[column];
			if(!groups.containsKey(key))groups.put(key, new ArrayList<String[]>());
			groups.get(key).add(row);
		}
		return groups;
	}

}
