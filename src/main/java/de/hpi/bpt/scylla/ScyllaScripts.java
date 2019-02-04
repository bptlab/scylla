package de.hpi.bpt.scylla;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * This class contains various useful scripts
 * @author Leon Bein
 *
 */
public class ScyllaScripts {
	
    
    public static void runAllBatchSimulations() {
    	String folder = "..\\batch_tests\\2019_jan_28\\";
    	try {
			Files.walk(Paths.get(folder))
				.filter(each -> {return each.toString().endsWith(".bpmn");})
				.forEach(each -> {
					String fileName = each.getName(each.getNameCount()-1).toString().split("\\.")[0];
					Path globalConf = each.resolveSibling("BatchGlobalConfiguration.xml");
					assert globalConf.toFile().exists();
					Path simConf = each.resolveSibling(fileName+".xml");
					assert simConf.toFile().exists();
					Path output = each.resolveSibling(fileName);
					runSimulation(globalConf.toString(), each.toString(), simConf.toString(), output.toString()+"\\");
			    	
				});
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    

    public static void replaceDistributions() {
    	
    	/*folder = "";
    	String f = "..\\batch_tests\\2019_jan_28\\";
    	try {
			Files.walk(Paths.get(folder+f))
				.filter(each -> {return each.toString().endsWith(".xml");})
				.filter(each -> {return !each.toString().contains("Global");})
				.forEach(each -> {
					try {
				        SAXBuilder builder = new SAXBuilder();
						Document doc = builder.build(each.toString());
				        Element root = doc.getRootElement();
				        System.out.println(each);
				        List<Element> distributions = new LinkedList<>();
				        root.getDescendants(new ElementFilter("exponentialDistribution")).forEach(distributions::add);
				        distributions.forEach(eDist -> {
				        	double mean = Double.parseDouble(eDist.getChildren().get(0).getText());
				        	Element uDist = new Element("uniformDistribution", root.getNamespace());
				        	Element lower = new Element("lower", root.getNamespace());
				        	lower.setText(""+mean);
				        	Element upper = new Element("upper", root.getNamespace());
				        	upper.setText(""+(mean+2));
				        	uDist.addContent(lower);
				        	uDist.addContent(upper);
				        	Element parent = eDist.getParentElement();
				        	parent.removeContent(eDist);
				        	parent.addContent(uDist);
				        });
				        
				        FileOutputStream fos = new FileOutputStream(each.toString());
				        XMLOutputter xmlOutput = new XMLOutputter();
				        xmlOutput.setFormat(Format.getPrettyFormat());
				        xmlOutput.output(doc, fos);
				        	
				        
					} catch (JDOMException | IOException e) {
						e.printStackTrace();
					}
				});
		} catch (IOException e) {
			e.printStackTrace();
		}*/
    	
    }
    

    public static void oldRunAllBatchSimulations() {
    	
    	/*runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version1_parallel.bpmn", 	f+"Version1.xml", 			f+"results\\v1_parallel\\");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version1_seqTaskbased.bpmn",	f+"Version1.xml", 			f+"results\\v1_taskbased\\");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version1_seqTaskbased.bpmn",	f+"Version1_casebased.xml", f+"results\\v1_casebased\\");
    	
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version2_parallel.bpmn", 	f+"Version2.xml", 			f+"results\\v2_parallel\\");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version2_seqTaskbased.bpmn", f+"Version2.xml", 			f+"results\\v2_taskbased\\");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version2_seqCasebased.bpmn", f+"Version2_casebased.xml", f+"results\\v2_casebased\\");
    	
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version3_parallel.bpmn", 	f+"Version3.xml", 			f+"results\\v3_parallel\\");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version3_seqTaskbased.bpmn", f+"Version3.xml", 			f+"results\\v3_taskbased\\");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version3_seqCasebased.bpmn", f+"Version3_casebased.xml", f+"results\\v3_casebased\\");*/
    }
    
    
    
    public static void runSimulation(String global, String bpmn, String sim, String outputPath) {
    	SimulationManager manager = new SimulationManager(null, 
				new String[] {bpmn}, 
				new String[] {sim}, 
				global, 
				true, 
				false);
		if(Objects.nonNull(outputPath))manager.setOutputPath(outputPath);
		manager.run();
    }

}
