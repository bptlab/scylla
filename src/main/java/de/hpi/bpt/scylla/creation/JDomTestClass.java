package de.hpi.bpt.scylla.creation;

import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable.TimetableItem;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution.DistributionType;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.ExclusiveGateway;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Task;

//TODO delete
public class JDomTestClass {
	
	public static void main(String[] args){
		createEmpty();
	}
	
	public static void createEmpty(){
		

	      FileWriter writer;
		
//		Namespace nsp = Namespace.getNamespace("bsim","http://bsim.hpi.uni-potsdam.de/scylla/simModel");
//		
//		Element root = new Element("globalConfiguration",nsp);
//		Document d = new Document(root);
//		root.setAttribute("targetNamespace", "http://www.hpi.de");
//		Element rAO = new Element("resourceAssignmentOrder",nsp);
//		rAO.setText("priority,simulationTime,");
//		root.addContent(rAO);
		
		GlobalConfigurationCreator c = new GlobalConfigurationCreator();
		c.setId("This is an ID");
		c.addReferencedResourceAssignmentOrder("priority");
		c.addReferencedResourceAssignmentOrder("simulationTime");
		c.removeReferencedResourceAssignmentOrder("simulationTime");
		c.setSeed(1337);
		c.setTimeOffset(ZoneOffset.ofHoursMinutesSeconds(13, 37, 42));

		c.createTimetable("Hero");
		Timetable lazy = c.createTimetable("Lazy");
		c.deleteTimetable("Hero");
		c.deleteTimetable("Hero");
		TimetableItem item = lazy.addItem(DayOfWeek.MONDAY, DayOfWeek.FRIDAY,LocalTime.parse("13:37"), LocalTime.parse("20:35:37"));
		item.setFrom(DayOfWeek.THURSDAY);

		c.addResourceType("Student");
		c.addResourceType("Professor");
		c.addResourceType("Student"); //Nothing should happen
		c.removeResourceType("Student");
		
		ResourceType student = c.addResourceType("Student");	
		
		student.setName("True Hero");
		student.setDefaultTimeUnit(TimeUnit.MINUTES);
		student.setDefaultQuantity(100);
		student.setDefaultCost(12.50);
		student.addInstance("Anton");
		student.addInstance("Bert");
		student.getInstance("Anton").setCost(50);
		student.removeInstance("Bert");
		
		ResourceType prof = c.getResourceType("Professor");
		prof.setDefaultQuantity(1);
		prof.setDefaultCost(41.14);
		prof.setDefaultTimeUnit(TimeUnit.HOURS);
		
		
		c.validate();
//		GlobalConfigurationCreator c = null;
//		try {
//			c = GlobalConfigurationCreator.createFromFile("testFile.xml");
//		} catch (JDOMException | IOException e1) {
//			e1.printStackTrace();
//		}
//		
//		c.validate();
//		
		try {
			writer = new FileWriter("testFile.xml");
	        XMLOutputter outputter = new XMLOutputter();
	        outputter.setFormat(Format.getPrettyFormat());
	        outputter.output(c.getDoc(), writer);
	        outputter.output(c.getDoc(), System.out);
	        //writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		
        Element r = null;
		try {
	        Document doc;
	        SAXBuilder builder = new SAXBuilder();
			doc = builder.build("./samples/p2_normal.bpmn");
	        r = doc.getRootElement();
		} catch (JDOMException | IOException e1){
			e1.printStackTrace();
		}
		
		SimulationConfigurationCreator s = new SimulationConfigurationCreator();
		s.setId("This is the id");
		//s.setProcessRef("Process_2");
		s.setProcessInstances(10);
		s.setStartDateTime(ZonedDateTime.parse("2017-07-06T09:00:00.000+02:00"));
		s.setEndDateTime(ZonedDateTime.parse("2017-07-12T09:00:00.000+02:00"));

		s.setRandomSeed(1337);
        s.setModel(r);
        
		Distribution testDistribution = Distribution.create(DistributionType.BINOMIAL);
		testDistribution.setAttribute("amount",5);
		testDistribution.setAttribute(0, 0.2);
		s.getStartEvent().setArrivalRateDistribution(testDistribution);
		
		Task t = (Task)s.getElement("Task_1tvvo6w");
		t.setDurationDistribution(Distribution.create(DistributionType.CONSTANT));
		t.getDurationDistribution().setAttribute("constantValue", 100);
		t.assignResource(student).setAmount(5);
		t.assignResource(prof).setAmount(5);
		
		t.getResource("Student").setAmount(13);
		t.deassignResource(prof.getId());
		t.getResource(student.getId()).setAssignmentPriority(5);

		t.assignResource(prof).setAmount(5);
		t.getResource("Professor").setAssignmentPriority(0);
		t.getResource(prof.getId()).removeAssignmentDefinition();
		t.deassignResource(prof.getId());
		
		for(ElementLink element : s.getElements()){
			if(!(element instanceof Task))continue;
			Task task = (Task)element;
			Distribution d = Distribution.create(DistributionType.TRIANGULAR);
			d.setAttribute(0,11);
			d.setAttribute(2,33);
			d.setAttribute("peak",22);
			task.setDurationDistribution(d);
			task.assignResource(prof).setAmount(3);
		}
		
		ExclusiveGateway g = null;
		for(ElementLink element : s.getElements()){
			if(element instanceof ExclusiveGateway){
				g = (ExclusiveGateway) element;
				break;
			}
		}
		g.setBranchingProbability("SequenceFlow_1237oxj", 1);
		
		for(String branch : g.getBranches()){
			System.err.println(s.getFlowTarget(branch).el.getAttributeValue("name"));
		}
		/*		SimulationConfigurationCreator s = null;
		try {
			s = SimulationConfigurationCreator.createFromFile("./samples/p2_normal_sim.xml", "./samples/p2_normal.bpmn");
		} catch (JDOMException | IOException e1) {
			e1.printStackTrace();
		}*/
		
		try {
			writer = new FileWriter("testFile2.xml");
	        XMLOutputter outputter = new XMLOutputter();
	        outputter.setFormat(Format.getPrettyFormat());
	        outputter.output(s.getDoc(), writer);
	        outputter.output(s.getDoc(), System.out);
	        //writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
