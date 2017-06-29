package de.hpi.bpt.scylla.creation;

import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable.TimetableItem;

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
		/*
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
		
		
		c.validate();*/
		GlobalConfigurationCreator c = null;
		try {
			c = GlobalConfigurationCreator.createFromFile("testFile.xml");
		} catch (JDOMException | IOException e1) {
			e1.printStackTrace();
		}
		
		c.validate();
		
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
	}
	

}
