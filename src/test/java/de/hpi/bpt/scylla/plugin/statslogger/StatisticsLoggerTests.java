package de.hpi.bpt.scylla.plugin.statslogger;

import de.hpi.bpt.scylla.SimulationTest;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static de.hpi.bpt.scylla.Scylla.normalizePath;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatisticsLoggerTests extends SimulationTest {

    @Test
    /**
     * See https://github.com/bptlab/scylla/issues/63
     */
    public void testArbitraryActivityInstancesRegression() throws IOException, JDOMException {
        runSimpleSimulation(
                "p0_globalconf.xml",
                "singleActivity.bpmn",
                "SimpleConfig.xml");

        File f = new File(normalizePath("./"+outputPath+getGlobalConfiguration().getFileNameWithoutExtension()+"_resourceutilization.xml"));

        SAXBuilder builder = new SAXBuilder();
        Element reportRoot = builder.build(f).getRootElement();
        Namespace namespace = reportRoot.getNamespace();
        Element activityInstances = reportRoot
                .getChild("processes", namespace)
                .getChildren().get(0) // There is exactly one process in the model
                .getChild("activities", namespace)
                .getChildren().get(0) // There is exactly one activity in the model
                .getChild("instances", namespace);
        assertEquals(getSimulationConfiguration().getNumberOfProcessInstances(), activityInstances.getChildren().size());
    }
    @Override
    protected String getFolderName() {
        return "core";
    }
}
