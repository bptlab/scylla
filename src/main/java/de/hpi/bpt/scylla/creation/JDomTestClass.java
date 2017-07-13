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
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable.TimetableItem;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution.DistributionType;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator;

//TODO delete
public class JDomTestClass {
	
	public static void main(String[] args){
		createEmpty();
	}
	
	public static void createEmpty(){
		
		
		
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
        FileWriter writer;
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
		
		SimulationConfigurationCreator s = new SimulationConfigurationCreator();
		s.setId("This is the id");
		s.setProcessRef("Process_2");
		s.setProcessInstances(10);
		s.setStartDateTime(ZonedDateTime.parse("2017-07-06T09:00:00.000+02:00"));
		s.setEndDateTime(ZonedDateTime.parse("2017-07-12T09:00:00.000+02:00"));
		s.setRandomSeed(1337);
		
		Distribution testDistribution = new Distribution(DistributionType.binomial);
		s.el.addContent(testDistribution.el);
		testDistribution.setAttribute("amount",5);
		testDistribution.setAttribute(0, 2.3);
		
        Document doc;
		try {
	        SAXBuilder builder = new SAXBuilder();
			doc = builder.build("./samples/p2_normal.bpmn");
	        Element r = doc.getRootElement();
	        s.setModel(r);
		} catch (JDOMException | IOException e1){
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
      //FileWriter writer;
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
