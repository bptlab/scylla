package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;

import de.hpi.bpt.scylla.TestUtils;

public class ThresholdRuleTests extends BatchSimulationTest {
	
	public static void main(String[] args) {
		//new ThresholdRuleTests().testDueDateDoesNotTriggerRegression();
		new ThresholdRuleTests().testTimeoutTriggersBeforeMaxbatchsize();
	}
	
	@Test
	public void testDueDateTriggers() {
		assertTimeout(Duration.ofSeconds(5), ()->{
			runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"DueDateModel.bpmn", 
				"DueDateConfiguration.xml");
		});
		table.stream()
			.filter(each -> each[1].equals("Batch Activity"))
			.forEach(each -> {
				assertEquals(each[6], "1");
				assertStartTime(Duration.ofMinutes(10).toMillis() + Duration.ofDays(6).toMillis(), each[3]);	
			}
		);
	}
	
	@Test
	public void testThresholdTriggersBeforeTimeout() {
		int timeout = 30 * 60*1000, arrival = 2 * 60*1000;
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfigurationFixedArrival.xml");
		int instancesPerCluster = getBatchActivity().getActivationRule().getThreshold(null, null);
		table.stream()
			.filter(each -> each[1].equals("Batch Activity"))
			.forEach(each -> {
				int instance = Integer.parseInt(each[0]);
				int expectedCluster = regularClusterOf(instance, instancesPerCluster);
				assertEquals(expectedCluster, Integer.parseInt(each[6]));
				if(expectedCluster != getSimulationConfiguration().getNumberOfProcessInstances()/instancesPerCluster + 1)
					assertStartTime((expectedCluster * instancesPerCluster - 1) * arrival, each[3]);
				else //last cluster is not filled => timeout
					assertStartTime(((expectedCluster - 1) * instancesPerCluster) * arrival + timeout, each[3]);
			}
		);
	}
	
	@Test
	public void testTimeoutTriggersBeforeMaxbatchsize() {
		int timeout = 30 * 60*1000, arrival = 2 * 60*1000;
		int instancesPerCluster = timeout/arrival;//Meaning that instances that arrive exactly at the timeout are EXcluded
		afterParsing(()->{
			TestUtils.setAttribute(getBatchActivity().getActivationRule(), "threshold", 20);
			TestUtils.setAttribute(getBatchActivity(), "maxBatchSize", 20);
		});
		runSimpleSimulation(
				"BatchTestGlobalConfiguration.xml", 
				"ModelSimple.bpmn", 
				"BatchTestSimulationConfigurationFixedArrival.xml");
		table.stream()
			.filter(each -> each[1].equals("Batch Activity"))
			.forEach(each -> {
				System.err.println(Arrays.toString(each));
				int instance = Integer.parseInt(each[0]);
				int expectedCluster = regularClusterOf(instance, instancesPerCluster);
				assertEquals(expectedCluster, Integer.parseInt(each[6]));
				assertStartTime(timeout * expectedCluster, each[3]);
			}
		);
	}
	
	private int regularClusterOf(int instance, int instancesPerCluster) {
		return (int)Math.ceil((double)instance/instancesPerCluster);
	}
	
	private void assertStartTime(long expectedOffsetMilli, String actualArrival) {
		Date expectedArrival = new Date(getSimulationConfiguration().getStartDateTime().toInstant().toEpochMilli() + expectedOffsetMilli);
		try {
			assertEquals(expectedArrival, BatchCSVLogger.timeFormat.parse(actualArrival));
		} catch (ParseException e) {
			fail(e);
		}
	}

}
