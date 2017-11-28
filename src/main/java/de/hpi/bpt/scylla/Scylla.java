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

    public static void main(String[] args) {
        /**
         * BEGIN of simulation scenarios
         */

        /**
         * Simulation scenarios to test plug-ins.
         */

        String configurationFile = args[0];
        String[] bpmnFilenames = Arrays.stream(Arrays.copyOfRange(args, 1, args.length))
                                                .filter(x -> x.contains(".bpmn"))
                                                .toArray(String[]::new);

        String[] simFilenames = Arrays.stream(Arrays.copyOfRange(args, 1, args.length))
                                                .filter(x -> x.contains(".xml"))
                                                .toArray(String[]::new);

        String outputFolder = args[args.length - 1];

    	/**
         * Simulation scenarios to test dmn simulation.
         */
    	
        /**
         * Simulation scenarios to test batch processes. May be used for regular simulation.
         */

        /**
         * END of simulation scenarios
         */

        boolean enableBpsLogging = true;
        boolean enableDesmojLogging = true;

        SimulationManager manager = new SimulationManager(outputFolder, bpmnFilenames, simFilenames, configurationFile,
                enableBpsLogging, enableDesmojLogging);
        manager.run();
    }

}
