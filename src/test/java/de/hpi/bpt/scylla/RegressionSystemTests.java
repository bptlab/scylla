package de.hpi.bpt.scylla;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegressionSystemTests extends SimulationTest {

	@Test
	/**
	 * When a model was run where the task duration exceeded timetable items and there was nanoseconds in the start-time,
	 * the simulation did not terminate
	 * because infinitey resource availability events were scheduled
	 * Solved by forbidding nano parts of simulation start times
	 */
	public void testTimetableDeadlockWhenNanoSecondsRegression() {
		Assertions.assertDoesNotThrow(() -> {
			String folder = "timetableDeadlockWhenNanoSecondsRegression/";
			CompletableFuture.runAsync(() -> {
				runSimpleSimulation(
						folder+"InsuranceCompanyConfiguration.xml", 
						folder+"claim_process_regular.bpmn", 
						folder+"claim_process_regular_configuration.xml");
			}).get(5, TimeUnit.SECONDS);
		}, "Running simple model with task duration exceeding timetable item caused deadlock.");
	}
	
	
	@Override
	protected String getFolderName() {
		return "core";
	}

}
