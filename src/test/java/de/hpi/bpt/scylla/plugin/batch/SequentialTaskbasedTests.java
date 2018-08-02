package de.hpi.bpt.scylla.plugin.batch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.TestUtils;

public class SequentialTaskbasedTests {
	
	public static void main(String[] args) {
		SequentialTaskbasedTests x = new SequentialTaskbasedTests();
		x.testActivitiesAreSequential();
	}
	
	private static final String folder = "src\\test\\resources\\BatchPlugin\\";
	private String outputPath;
	
	@Before
	public void setUp() {
		System.setErr(new PrintStream(new ByteArrayOutputStream()));
		System.setOut(new PrintStream(new ByteArrayOutputStream()));
	}
	
	@Test(timeout=3000)
	public void testActivitiesAreSequential() {
		outputPath = new SimulationManager(
				folder, 
				new String[] {folder+"SimpleTaskbasedModel.bpmn"}, 
				new String[] {folder+"BatchTestSimulationConfiguration.xml"}, 
				folder+"BatchTestGlobalConfiguration.xml",
                true, false).run();
		File f = new File(".\\"+outputPath+"Process_1_processBatchActivityStats.csv");
		Assert.assertTrue(f.exists());
		List<String[]> table = TestUtils.readCSV(f);
		Assert.assertEquals(30, table.size());
		Map<String, List<String[]>> clusters = new HashMap<String, List<String[]>>();
		for(String[] row : table) {
			String activity = row[1];
			if(!(activity.equals("Activity A") || activity.equals("Activity B")))continue;
			String batchNumber = row[6];
			if(!clusters.containsKey(batchNumber))clusters.put(batchNumber, new ArrayList<String[]>());
			clusters.get(batchNumber).add(row);
		}
		for(List<String[]> cluster : clusters.values()) {
			for(int i = 0; i < cluster.size(); i+=2) {
				//Assert that every A activity has been ended before the B activity has started
				Assert.assertTrue(cluster.get(i)[4].compareTo(cluster.get(i+1)[3]) <= 0);
			}
			cluster.sort((a,b) -> {return a[3].compareTo(b[3]);});
			for(int i = 0; i < cluster.size()-1; i++) {
				String[] current = cluster.get(i);
				String[] next = cluster.get(i+1);
				//Assert that there is no A activity after a B activity
				if(current[1].equals("Activity B"))Assert.assertEquals("Activity B",next[1]);
				//Assert that the activities are executed sequentially
				Assert.assertTrue(current[4].compareTo(next[3]) <= 0);
			}
		}
	}
	
	@After
	public void tearDown() {
		TestUtils.deleteFolder(new File(".\\"+outputPath));
		TestUtils.cleanupOutputs(".\\"+folder);
	}

}
