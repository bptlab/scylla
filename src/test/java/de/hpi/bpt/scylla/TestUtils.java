package de.hpi.bpt.scylla;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentest4j.AssertionFailedError;

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
	
	public static Map<String, List<String[]>> groupBy(List<String[]> table, int column){
		Map<String, List<String[]>> groups = new HashMap<String, List<String[]>>();
		for(String[] row : table) {
			String key = row[column];
			if(!groups.containsKey(key))groups.put(key, new ArrayList<String[]>());
			groups.get(key).add(row);
		}
		return groups;
	}
	
	public static void assertAttribute(Object o, String attributeName, Object expectedValue) {
		try {
			Field field = o.getClass().getDeclaredField(attributeName);
			field.setAccessible(true);
			Object actualValue = field.get(o);
			assertEquals(expectedValue, actualValue);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new AssertionFailedError("Could not validate attribute "+attributeName+" in class named "+o.getClass().getSimpleName()+": Not existent or not accessible!", e);
		}
	}
	
	public static void setAttribute(Object o, String attributeName, Object value) {
		try {
			Field field = o.getClass().getDeclaredField(attributeName);
			field.setAccessible(true);
			field.set(o, value);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

}
