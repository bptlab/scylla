package de.hpi.bpt.scylla;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
	
	public static <ValueType> Map<ValueType, List<ValueType[]>> groupBy(List<ValueType[]> table, int column){
		return groupBy(table, row -> row[column]);
	}
	
	public static <KeyType,Row> Map<KeyType, List<Row>> groupBy(List<Row> table, Function<Row, KeyType> keyFunction){
		Map<KeyType, List<Row>> groups = new HashMap<KeyType, List<Row>>();
		for(Row row : table) {
			KeyType key = keyFunction.apply(row);
			groups.putIfAbsent(key, new ArrayList<Row>());
			groups.get(key).add(row);
		}
		return groups;
	}
	
	public static void assertAttribute(Object o, String attributeName, Object expectedValue) {
		try {
			Field field = getFieldNamed(o.getClass(), attributeName);
			field.setAccessible(true);
			Object actualValue = field.get(o);
			assertEquals(expectedValue, actualValue);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new AssertionFailedError("Could not validate attribute "+attributeName+" in class named "+o.getClass().getSimpleName()+": Not existent or not accessible!", e);
		}
	}
	
	public static void setAttribute(Object o, String attributeName, Object value) {
		try {
			Field field = getFieldNamed(o.getClass(), attributeName);
			field.setAccessible(true);
			field.set(o, value);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private static Field getFieldNamed(Class<?> c, String name) throws NoSuchFieldException {
		if(c == null) throw new NoSuchFieldException(name);
		if(Arrays.stream(c.getDeclaredFields()).anyMatch(each -> each.getName().equals(name))) {
			return c.getDeclaredField(name);
		} else {
			return getFieldNamed(c.getSuperclass(), name);
		}
		
	}

}
