package de.hpi.bpt.scylla;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimetableTests extends SimulationTest {


    @Test
    public void testUnnamedInstancesUseDefaultTimetable() throws IOException, JDOMException {
        removeNamedInstances();
        patchArrivalRate();
        runSimpleSimulation(
                "TimeTableTestGlobalConf.xml",
                "singleActivity.bpmn",
                "SimpleConfig.xml");

        getEventLogEvents()
            .filter(TimetableTests::hasResourceAssigned)
            .map(TimetableTests::getTimeStamp)
            .forEach(timestamp -> assertEquals(DayOfWeek.MONDAY, timestamp.getDayOfWeek()));
    }

    @Test
    public void testNamedInstancesWithoutOverrideUseDefaultTimetable() throws IOException, JDOMException {
        patchArrivalRate();
        runSimpleSimulation(
                "TimeTableTestGlobalConf.xml",
                "singleActivity.bpmn",
                "SimpleConfig.xml");

        getEventLogEvents()
                .filter(hasSpecificResourceAssigned("Student_Using_Default_Timetable"))
                .map(TimetableTests::getTimeStamp)
                .forEach(timestamp -> assertEquals(DayOfWeek.MONDAY, timestamp.getDayOfWeek()));
    }

    @Test
    public void testNamedInstancesWithOverrideUseOverrideTimetable() throws IOException, JDOMException {
        patchArrivalRate();
        runSimpleSimulation(
                "TimeTableTestGlobalConf.xml",
                "singleActivity.bpmn",
                "SimpleConfig.xml");

        getEventLogEvents()
                .filter(hasSpecificResourceAssigned("Student_Overriding_Default_Timetable"))
                .map(TimetableTests::getTimeStamp)
                .forEach(timestamp -> assertEquals(DayOfWeek.TUESDAY, timestamp.getDayOfWeek()));
    }

    static boolean hasResourceAssigned(Element xesEvent) {
        return xesEvent.getChildren("string").stream()
                .anyMatch(string -> string.getAttributeValue("key").toString().contains("resource"));
    }

    static Predicate<Element> hasSpecificResourceAssigned(String resourceName) {
        return xesEvent -> xesEvent.getChildren("string").stream()
                .filter(string -> string.getAttributeValue("key").toString().contains("resource"))
                .map(resourceAssignment -> resourceAssignment.getAttributeValue("value").equals(resourceName))
                .findFirst()
                .orElse(false);
    }

    static ZonedDateTime getTimeStamp(Element xesEvent) {
        Element timestampElement = xesEvent.getChild("date");
        return ZonedDateTime.parse(timestampElement.getAttributeValue("value"));
    }


    public void patchArrivalRate() {
        beforeParsingSims.computeIfAbsent("SimpleConfig", s -> new ArrayList<>()).add(() -> {
            Element root = simConfigRoots.get("SimpleConfig");
            Namespace nsp = root.getNamespace();
            Element arrivalRateElement = root.getChild("startEvent", nsp).getChild("arrivalRate", nsp);
            arrivalRateElement.setAttribute("timeUnit", TimeUnit.DAYS.toString());
            arrivalRateElement.getChild("constantDistribution", nsp).getChild("constantValue", nsp).setText("1");
        });
    }

    public void removeNamedInstances() {
        beforeParsingGlobal.add(() -> {
            Namespace nsp = globalConfigRoot.getNamespace();
            globalConfigRoot.getChild("resourceData", nsp).getChildren("dynamicResource", nsp).forEach(dynamicResource -> {
                dynamicResource.removeChildren("instance", nsp);
            });
        });
    }

    @Override
    protected String getFolderName() {
        return "core";
    }
}
