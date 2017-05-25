package de.hpi.bpt.scylla;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.global.GlobalConfiguration;
import de.hpi.bpt.scylla.model.process.CommonProcessElements;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.parser.CommonProcessElementsParser;
import de.hpi.bpt.scylla.parser.GlobalConfigurationParser;
import de.hpi.bpt.scylla.parser.ProcessModelParser;
import de.hpi.bpt.scylla.parser.SimulationConfigurationParser;
import de.hpi.bpt.scylla.plugin_type.logger.OutputLoggerPluggable;
import de.hpi.bpt.scylla.plugin_type.parser.CommonProcessElementsParserPluggable;
import de.hpi.bpt.scylla.plugin_type.parser.GlobalConfigurationParserPluggable;
import de.hpi.bpt.scylla.plugin_type.parser.ProcessModelParserPluggable;
import de.hpi.bpt.scylla.plugin_type.parser.SimulationConfigurationParserPluggable;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;

/**
 * Simulation manager which controls the overall process of simulation, from input parsing to output logging.
 * 
 * @author Tsun Yin Wong
 *
 */
public class SimulationManager {

    private String experimentOutputFolder;

    private GlobalConfiguration globalConfiguration;
    private Map<String, CommonProcessElements> commonProcessElements = new HashMap<String, CommonProcessElements>();
    private Map<String, ProcessModel> processModels = new HashMap<String, ProcessModel>();
    private Map<String, SimulationConfiguration> simulationConfigurations = new HashMap<String, SimulationConfiguration>();

    private boolean enableBpsLogging;
    private boolean enableDesLogging;

    private String[] processModelFilenames;
    private String[] simulationConfigurationFilenames;
    private String globalConfigurationFilename;
    
	private String outputPath;

    /**
     * Constructor.
     * 
     * @param folder
     *            location of files
     * @param processModelFilenames
     *            process model filenames in given folder
     * @param simulationConfigurationFilenames
     *            simulation configuration filenames in given folder
     * @param globalConfigurationFilename
     *            global configuration filename in given folder
     * @param enableBpsLogging
     *            log {@link de.hpi.bpt.scylla.logger.ProcessNodeInfo} objects if true
     * @param enableDesLogging
     *            log DesmoJ traces and write HTML trace file if true
     * @param gui
     * 			a gui reference for feedback
     */
    public SimulationManager(String folder, String[] processModelFilenames, String[] simulationConfigurationFilenames,
            String globalConfigurationFilename, boolean enableBpsLogging, boolean enableDesLogging) {

        this.experimentOutputFolder = folder;
        this.processModelFilenames = processModelFilenames;
        this.simulationConfigurationFilenames = simulationConfigurationFilenames;
        this.globalConfigurationFilename = globalConfigurationFilename;
        this.enableBpsLogging = enableBpsLogging;
        this.enableDesLogging = enableDesLogging;
    }

    /**
     * parses input, runs DesmoJ simulation experiment, writes BPS output logs
     */
    public void run() {

        try {
            SAXBuilder builder = new SAXBuilder();
            
            if (globalConfigurationFilename == null) {
                throw new ScyllaValidationException("No global configuration provided.");
            }
            else {
                // parse global configuration XML
                Document gcDoc = builder.build(globalConfigurationFilename);
                Element gcRootElement = gcDoc.getRootElement();
                GlobalConfigurationParser globalConfigurationParser = new GlobalConfigurationParser(this);
                globalConfiguration = globalConfigurationParser.parse(gcDoc.getRootElement());
                String fileNameWithoutExtension = globalConfigurationFilename.substring(// filename.lastIndexOf("\\") +
                                                                                        // 1,
                		globalConfigurationFilename.lastIndexOf("\\")+1, globalConfigurationFilename.lastIndexOf(".xml"));
                globalConfiguration.setFileNameWithoutExtension(fileNameWithoutExtension);
                // plugins to parse global configuration
                GlobalConfigurationParserPluggable.runPlugins(this, globalConfiguration, gcRootElement);

                DateTimeUtils.setZoneId(globalConfiguration.getZoneId());
            }

            CommonProcessElementsParser cpeParser = new CommonProcessElementsParser(this);
            for (String filename : processModelFilenames) {
                Document pmDoc = builder.build(filename);
                Element pmRootElement = pmDoc.getRootElement();

                // parse common process elements from XML (BPMN)
                CommonProcessElements commonProcessElementsFromFile = cpeParser.parse(pmRootElement);
                String fileNameWithoutExtension = filename.substring(// filename.lastIndexOf("\\") + 1,
                        filename.lastIndexOf("\\")+1, filename.lastIndexOf(".bpmn"));
                commonProcessElementsFromFile.setBpmnFileNameWithoutExtension(fileNameWithoutExtension);

                // plugins to parse common process elements
                CommonProcessElementsParserPluggable.runPlugins(this, commonProcessElementsFromFile, pmRootElement);

                // parse process model(s) from XML (BPMN)
                ProcessModelParser pmParser = new ProcessModelParser(this);
                pmParser.setCommonProcessElements(commonProcessElementsFromFile);
                ProcessModel processModelFromFile = pmParser.parse(pmDoc.getRootElement());
                String processId = processModelFromFile.getId();
                if (processModels.containsKey(processId)) {
                    throw new ScyllaValidationException("Duplicate process model with id " + processId + ".");
                }

                // plugins to parse process model(s)
                ProcessModelParserPluggable.runPlugins(this, processModelFromFile, pmRootElement);

                processModels.put(processId, processModelFromFile);
                commonProcessElements.put(processId, commonProcessElementsFromFile);
            }

            SimulationConfigurationParser simParser = new SimulationConfigurationParser(this);
            // parse each simulation configuration XML
            for (String filename : simulationConfigurationFilenames) {
                Document scDoc = builder.build(filename);
                SimulationConfiguration simulationConfigurationFromFile = simParser.parse(scDoc.getRootElement());
                String processId = simulationConfigurationFromFile.getProcessModel().getId();
                if (simulationConfigurations.containsKey(processId)) {
                    throw new ScyllaValidationException(
                            "Multiple simulation configurations for process with id " + processId + ".");
                }

                // plugins to parse simulation configuration
                SimulationConfigurationParserPluggable.runPlugins(this, simulationConfigurationFromFile, scDoc);

                simulationConfigurations.put(processId, simulationConfigurationFromFile);
            }
        }
        catch (JDOMException | IOException | ScyllaValidationException e) {
            DebugLogger.error(e.getMessage());
            e.printStackTrace();
        }

        // TODO validate resources in process models (i.e. check if they are all covered in resource data)

        TimeUnit epsilon = TimeUnit.SECONDS;
        DateTimeUtils.setReferenceTimeUnit(epsilon);

        String experimentName = Long.toString((new Date()).getTime());
        Experiment.setEpsilon(epsilon);
        Experiment exp = new Experiment(experimentName, experimentOutputFolder);
        exp.setShowProgressBar(false);

        // XXX each simulation configuration may have its own seed
        Long randomSeed = globalConfiguration.getRandomSeed();
        if (randomSeed != null) {
            exp.setSeedGenerator(randomSeed);
        }
        else {
            exp.setSeedGenerator((new Random()).nextLong());
        }

        SimulationModel sm = new SimulationModel(null, globalConfiguration, commonProcessElements, processModels,
                simulationConfigurations, enableBpsLogging, enableDesLogging);
        sm.connectToExperiment(exp);

        int lambda = 1;

        if (sm.getEndDateTime() != null) {
            // have to use time which is slightly after intended end time (epsilon)
            // otherwise the AbortProcessSimulationEvent(s) may not fire
            long simulationDuration = DateTimeUtils.getDuration(sm.getStartDateTime(), sm.getEndDateTime());
            TimeInstant simulationTimeInstant = new TimeInstant(simulationDuration + lambda, epsilon);
            exp.stop(simulationTimeInstant);
            exp.tracePeriod(new TimeInstant(0), simulationTimeInstant);
            exp.debugPeriod(new TimeInstant(0), simulationTimeInstant);
        }
        else {
            exp.traceOn(new TimeInstant(0));
            exp.debugOn(new TimeInstant(0));
        }

        if (!enableDesLogging) {
            exp.debugOff(new TimeInstant(0));
            exp.traceOff(new TimeInstant(0));
        }

        exp.start();
        exp.report();
        exp.finish();

        try {

            // log process execution
            // log resources, process, tasks

            StringBuilder strb = new StringBuilder(globalConfigurationFilename.substring(0,globalConfigurationFilename.lastIndexOf("\\")+1));
            outputPath = strb.substring(0,strb.lastIndexOf("\\")+1)+"output_"+new SimpleDateFormat("yy_MM_dd_HH_mm_ss").format(new Date())+"\\";
            File outputPathFolder = new File(outputPath);
            if(!outputPathFolder.exists())outputPathFolder.mkdir();
            OutputLoggerPluggable.runPlugins(sm, outputPath);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    public Map<String, ProcessModel> getProcessModels() {
        return processModels;
    }

    /**
     * Returns default output path if set
     * @return
     */
	public String getOutputPath() {
		return outputPath;
	}

	/**
	 * Methode to manually override default output path
	 * @param outputPath : A String path to a folder
	 */
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
}
