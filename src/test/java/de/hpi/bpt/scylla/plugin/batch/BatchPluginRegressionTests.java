package de.hpi.bpt.scylla.plugin.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BatchPluginRegressionTests extends BatchSimulationTest {

    @Test
    public void testResourceBasedNullpointerInBatchTaskTerminateAddToLogRegression() {
        // Happened when a process instance was added to cluster between cluster enablement and start events
        setGlobalSeed(-9088523333451316106L);
        assertDoesNotThrow(() -> {
            runSimpleSimulation(
                    "BatchTestGlobalConfiguration.xml",
                    "ModelBatchTask.bpmn",
                    "BatchTestSimulationConfigurationBatchTaskWithResources.xml");
        });
    }

}
