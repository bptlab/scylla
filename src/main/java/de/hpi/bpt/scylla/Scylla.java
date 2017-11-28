package de.hpi.bpt.scylla;

import java.util.Arrays;

/**
 * Scylla is an extensible simulator for business processes in BPMN. <br>
 * This class initializes and runs the {@link SimulationManager} which simulates the processes based on the given input.
 * 
 * @author Tsun Yin Wong
 *
 */
public class Scylla {

    public static void main(String[] args) throws Exception {
        /**
         * BEGIN of simulation scenarios
         */

        /**
         * Simulation scenarios to test plug-ins.
         */

        String configurationFile = Arrays.stream(args)
                                                .filter(x -> x.contains("--config"))
                                                .map(s -> {
                                                        String[] splitted = s.split("=");
                                                        return splitted[splitted.length - 1];
                                                })
                                                .findFirst()
                                                .orElseThrow(() -> new IllegalArgumentException("You have to provide a configuration file. Usage: --config=<your file path>"));

        String[] bpmnFilenames = Arrays.stream(args)
                                                .filter(x -> x.contains("--bpmn"))
                                                .map(s -> {
                                                        String[] splitted = s.split("=");
                                                        return splitted[splitted.length - 1];
                                                })
                                                .toArray(String[]::new);

        if (bpmnFilenames.length == 0) {
                throw new Exception("You have to provide at least one bpmn diagram file. Usage: --bpmn=<your file path>");
        }

        String[] simFilenames = Arrays.stream(args)
                                                .filter(x -> x.contains(".xml"))
                                                .map(s -> {
                                                        String[] splitted = s.split("=");
                                                        return splitted[splitted.length - 1];
                                                })
                                                .toArray(String[]::new);

        if (simFilenames.length == 0) {
                throw new Exception("You have to provide at least one simulation file. Usage: --sim=<your file path>");
        }

        boolean enableBpsLogging = Arrays.stream(args).anyMatch(x -> "--enable-bps-logging".equalsIgnoreCase(x));
        boolean enableDesmojLogging = Arrays.stream(args).anyMatch(x -> "--desmoj-logging".equalsIgnoreCase(x));

        String outputFolder = Arrays.stream(args)
                                        .filter(x -> x.contains("--output"))
                                        .map(s -> {
                                                String[] splitted = s.split("=");
                                                return splitted[splitted.length - 1];
                                        })
                                        .findFirst()
                                        .orElse("sim-out/");
        
    	/**
         * Simulation scenarios to test dmn simulation.
         */
    	
        /**
         * Simulation scenarios to test batch processes. May be used for regular simulation.
         */

        /**
         * END of simulation scenarios
         */

        SimulationManager manager = new SimulationManager(outputFolder, bpmnFilenames, simFilenames, configurationFile,
                enableBpsLogging, enableDesmojLogging);
        manager.run();
    }

}
