package de.hpi.bpt.scylla;

import java.util.LinkedList;
import java.util.List;

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
    	 * Command line argument parsing
    	 */
    	
    	// current directory default output folder
    	String outputFolder = "";
    	List<String> processModels = new LinkedList<String>();
    	List<String> simulationConfigurations = new LinkedList<String>();
    	String globalConfiguration = null;
    	for (int i = 0; i < args.length; i++) {
    		String arg = args[i];
    		System.out.println(i + ": " + args[i]);
    		if (arg.equals("-p")) {
    			processModels = readFilenames(args, i+1);
    			i += processModels.size();
    		} else if (arg.equals("-s")) {
    			simulationConfigurations = readFilenames(args, i+1);
    			i += simulationConfigurations.size();
    		} else if (arg.equals("-g")) {
    			i++;
    			if (i < args.length) {
    				globalConfiguration = args[i];
    			}
    		} else if (arg.equals("-o")) {
    			i++;
    			if (i < args.length) {
    				outputFolder = args[i];
    			}
    		}
    	}
    	
    	if (processModels.isEmpty() || simulationConfigurations.isEmpty() || globalConfiguration == null || globalConfiguration.isEmpty()) {
    		System.err.println("Usage: scylla -o [output folder] -p [process model bpmns] -s [simulation configuration xmls] -g [global configuration xml]");
    		System.exit(1);
    	}
    	
        /**
         * BEGIN of simulation scenarios
         */

        /**
         * Simulation scenarios to test plug-ins.
         */

        // -p p1_boundary.bpmn p2_normal.bpmn p3_subproc.bpmn -s p1_boundary_sim.xml p2_normal_sim.xml p3_subproc_sim.xml -g p0_globalconf_without.xml

        // -p p4_parallelx.bpmn -s p4_parallel_sim.xml -g p0_globalconf_without.xml

        /**
         * Simulation scenarios to test batch processes. May be used for regular simulation.
         */

        // -p p5_batch.bpmn p6_return.bpmn -s p5_batch_sim.xml p6_return_sim.xml -g p56_conf_thesis.xml

        // -p p61_return_batch.bpmn -s p61_return_batch_sim.xml -g p61_conf_batch.xml

        /**
         * Simulation scenarios to test dmn simulation.
         */

        // -p p7_dmn.bpmn -s p7_dmn_sim.xml -g p0_globalconf_without.xml

        /**
         * END of simulation scenarios
         */

        boolean enableBpsLogging = true;
        boolean enableDesmojLogging = true;

        SimulationManager manager = new SimulationManager(outputFolder, processModels.toArray(new String[0]), simulationConfigurations.toArray(new String[0]), globalConfiguration,
                enableBpsLogging, enableDesmojLogging);
        manager.run();
    }
    
    private static List<String> readFilenames(String[] args, int index) {
    	List<String> filenames = new LinkedList<String>();
    	if (index >= args.length)
    		return filenames;
    	String arg = args[index];
    	while (!arg.equals("-o") && !arg.equals("-p") && !arg.equals("-s") && !arg.equals("-g")) {
    		filenames.add(arg);
    		index += 1;
    		if (index < args.length) {
    			arg = args[index];
    		} else {
    			break;
    		}
    	}
    	return filenames;
    }

}
