package de.hpi.bpt.scylla;

import static de.hpi.bpt.scylla.Scylla.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.hpi.bpt.scylla.plugin_loader.PluginLoader;

/**
 * This class contains various useful scripts for the programmatic usage of scylla.<br>
 * These are results from previous usages of the system
 * @author Leon Bein
 *
 */
public class ScyllaScripts {
	
	public static void main(String[] args) {
		runMoocModels();
	}
	
	public static void runMoocModels() {
		
    	PluginLoader.getDefaultPluginLoader().activateNone()
			.activatePackage("de.hpi.bpt.scylla.plugin.gateway_exclusive")
			.activatePackage("de.hpi.bpt.scylla.plugin.eventArrivalRate")
			.activatePackage("de.hpi.bpt.scylla.plugin.xeslogger")
			.activatePackage("de.hpi.bpt.scylla.plugin.statslogger_nojar")
			.printPlugins();


    	String f = "./samples/process_mining_mooc/";
    	//int[] clerkCountsToTest = new int[] {150, 172, 200};
    	int[] clerkCountsToTest = new int[] {5000};
    	int numInstances = 3000;
		String globalConf = f+"InsuranceCompanyConfiguration.xml";
		String model = f+"claim_process_with_noise.bpmn";
		String simConf = f+"claim_process_with_noise_configuration.xml";

		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(simConf);
			Element root = doc.getRootElement();
			root.getDescendants(new ElementFilter("simulationConfiguration")).forEach(node -> node.setAttribute("processInstances", ""+numInstances));
			FileOutputStream fos = new FileOutputStream(simConf);
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, fos);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
    	
    	for(int numClerks : clerkCountsToTest) {
    		try {
    	        SAXBuilder builder = new SAXBuilder();
    			Document doc = builder.build(globalConf);
    	        Element root = doc.getRootElement();
    	        List<Element> resources = new ArrayList<>();
    	        root.getDescendants(new ElementFilter("dynamicResource")).forEach(resources::add);
    	        resources.forEach(resource -> {
    	        	resource.setAttribute("defaultQuantity", ""+numClerks);
    	        });
    	        
    	        FileOutputStream fos = new FileOutputStream(globalConf);
    	        XMLOutputter xmlOutput = new XMLOutputter();
    	        xmlOutput.setFormat(Format.getPrettyFormat());
    	        xmlOutput.output(doc, fos);
    	        	
    	        
    		} catch (JDOMException | IOException e) {
    			e.printStackTrace();
    		}
        	
        	
        	runSimulation(
        			globalConf, 
        			model, 
        			simConf,	
        			f+"results/"+numClerks+"_"+numInstances+"_"+new SimpleDateFormat("yy_MM_dd_HH_mm_ss_SSS").format(new Date())+"/");
    	}
	}
	
    
    public static void runAllBatchSimulations() {
    	String folder = normalizePath("../batch_tests/2019_jan_28/");
    	try {
			Files.walk(Paths.get(folder))
				.filter(each -> {return each.toString().endsWith(".bpmn");})
				.forEach(each -> {
					String fileName = each.getName(each.getNameCount()-1).toString().split(FILEDELIM)[0];
					Path globalConf = each.resolveSibling("BatchGlobalConfiguration.xml");
					assert globalConf.toFile().exists();
					Path simConf = each.resolveSibling(fileName+".xml");
					assert simConf.toFile().exists();
					Path output = each.resolveSibling(fileName);
					runSimulation(globalConf.toString(), each.toString(), simConf.toString(), output.toString()+FILEDELIM);
			    	
				});
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    

    public static void replaceDistributions() {
    	
    	/*folder = "";
    	String f = "../batch_tests/2019_jan_28/";
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
    	
    	/*runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version1_parallel.bpmn", 	f+"Version1.xml", 			f+"results/v1_parallel/");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version1_seqTaskbased.bpmn",	f+"Version1.xml", 			f+"results/v1_taskbased/");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version1_seqTaskbased.bpmn",	f+"Version1_casebased.xml", f+"results/v1_casebased/");
    	
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version2_parallel.bpmn", 	f+"Version2.xml", 			f+"results/v2_parallel/");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version2_seqTaskbased.bpmn", f+"Version2.xml", 			f+"results/v2_taskbased/");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version2_seqCasebased.bpmn", f+"Version2_casebased.xml", f+"results/v2_casebased/");
    	
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version3_parallel.bpmn", 	f+"Version3.xml", 			f+"results/v3_parallel/");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version3_seqTaskbased.bpmn", f+"Version3.xml", 			f+"results/v3_taskbased/");
    	runSimulation(f+"BatchGlobalConfiguration.xml", f+"Version3_seqCasebased.bpmn", f+"Version3_casebased.xml", f+"results/v3_casebased/");*/
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
