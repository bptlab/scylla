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
    public static final String OS = System.getProperty("os.name");
    public static final String FILEDELIM = (OS.contains("Linux") || OS.contains("Mac OS")) ? "/" : "\\";
  
    public static void main(String[] args) throws Exception {

        if (Arrays.stream(args).anyMatch(x -> x.contains("--help"))) {
                System.out.println("Usage: Scylla --config=<your config file> --bpmn=<your first bpmn file> [--bpmn=<your second bpmn file>] [--bpmn=...] --sim=<your first sim file> [--sim=<your second sim file>] [--sim=...] [--output=<your output path>]");
                return;
        }

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
                throw new IllegalArgumentException("You have to provide at least one bpmn diagram file. Usage: --bpmn=<your file path>");
        }

        String[] simFilenames = Arrays.stream(args)
                                                .filter(x -> x.contains("--sim"))
                                                .map(s -> {
                                                        String[] splitted = s.split("=");
                                                        return splitted[splitted.length - 1];
                                                })
                                                .toArray(String[]::new);

        if (simFilenames.length == 0) {
                throw new IllegalArgumentException("You have to provide at least one simulation file. Usage: --sim=<your file path>");
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
        
        SimulationManager manager = new SimulationManager(outputFolder, bpmnFilenames, simFilenames, configurationFile,
                enableBpsLogging, enableDesmojLogging);
        manager.run();
    }

}