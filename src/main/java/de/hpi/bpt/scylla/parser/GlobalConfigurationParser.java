package de.hpi.bpt.scylla.parser;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.global.GlobalConfiguration;
import de.hpi.bpt.scylla.model.global.resource.DynamicResource;
import de.hpi.bpt.scylla.model.global.resource.DynamicResourceInstance;
import de.hpi.bpt.scylla.model.global.resource.Resource;
import de.hpi.bpt.scylla.model.global.resource.TimetableItem;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_type.parser.EventOrderType;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;

/**
 * Parses all simulation parameters which are necessary for conducting the simulation, across all involved business
 * processes.
 * 
 * @author Tsun Yin Wong
 */
public class GlobalConfigurationParser extends Parser<GlobalConfiguration> {

    public GlobalConfigurationParser(SimulationManager simulationEnvironment) {
        super(simulationEnvironment);
    }

    @Override
    public GlobalConfiguration parse(Element rootElement) throws ScyllaValidationException {
    	System.out.println(rootElement.getNamespace());

        Iterator<EventOrderType> eventOrderTypesIterator = PluginLoader.dGetPlugins(EventOrderType.class);
        //ServiceLoader.load(EventOrderType.class).iterator();
        //Get all event order type plugins and store them in eventOrderTypes
        Map<String, EventOrderType> eventOrderTypes = new HashMap<String, EventOrderType>();
        while (eventOrderTypesIterator.hasNext()) {
            EventOrderType eot = eventOrderTypesIterator.next();
            eventOrderTypes.put(eot.getName(), eot);
        }

        Namespace bsimNamespace = rootElement.getNamespace();
        List<Element> globalConfigurationElements = rootElement.getChildren(null, bsimNamespace);

        String globalConfId = rootElement.getAttributeValue("id");
        Long randomSeed = null;
        ZoneId zoneId = ZoneId.of("UTC");
        Map<String, Resource> resources = new HashMap<String, Resource>();
        List<EventOrderType> resourceAssignmentOrder = new ArrayList<EventOrderType>();

        // resourceId:[instanceName:timetableId]
        Map<String, Map<String, String>> resourcesToTimetableIds = new HashMap<String, Map<String, String>>();
        Map<String, List<TimetableItem>> timetables = new HashMap<String, List<TimetableItem>>();

        for (Element el : globalConfigurationElements) {
            String elementName = el.getName();
            if (isKnownElement(elementName)) {
                if (el.getText().isEmpty()) {
                    continue;
                }
                if (elementName.equals("resourceAssignmentOrder")) {
                    String resourceAssignmentOrderString = el.getText();
                    String[] orderTypeArray = resourceAssignmentOrderString.split(",");
                    for (String orderTypeName : orderTypeArray) {
                        if (orderTypeName.isEmpty()) {
                            continue;
                        }
                        EventOrderType eventOrderType = eventOrderTypes.get(orderTypeName);
                        if (eventOrderType == null) {
                            throw new ScyllaValidationException(
                                    "Event order type " + orderTypeName + " for resource assignment is unknown.");
                        }
                        resourceAssignmentOrder.add(eventOrderType);
                    }
                }
                else if (elementName.equals("randomSeed")) {
                    randomSeed = Long.parseLong(el.getText());
                }
                else if (elementName.equals("zoneOffset")) {
                    zoneId = ZoneId.of("GMT" + el.getText());
                }
                else if (elementName.equals("resourceData")) {
                    List<Element> rDataElements = el.getChildren();
                    for (Element elem : rDataElements) {
                        String resourceId = elem.getAttributeValue("id");
                        String rDataElementName = elem.getName();
                        if (rDataElementName.equals("dynamicResource")) {
                            String resourceName = elem.getAttributeValue("name");
                            Integer defaultQuantity = Integer.valueOf(elem.getAttributeValue("defaultQuantity"));
                            Double defaultCost = Double.valueOf(elem.getAttributeValue("defaultCost"));
                            TimeUnit defaultTimeUnit = TimeUnit.valueOf(elem.getAttributeValue("defaultTimeUnit"));
                            DynamicResource dynamicResource = new DynamicResource(resourceId, resourceName,
                                    defaultQuantity, defaultCost, defaultTimeUnit);

                            String defaultTimetableId = elem.getAttributeValue("defaultTimetableId");

                            if (resourcesToTimetableIds.containsKey(resourceId)) {
                                throw new ScyllaValidationException("Multiple resource definitions: " + resourceId);
                            }
                            resourcesToTimetableIds.put(resourceId, new HashMap<String, String>());

                            Map<String, DynamicResourceInstance> resourceInstances = dynamicResource
                                    .getResourceInstances();
                            List<Element> instanceElements = elem.getChildren("instance", bsimNamespace);

                            // fill up list of resource instances if not explicitly defined
                            if (instanceElements.size() > defaultQuantity) {
                                throw new ScyllaValidationException(
                                        "Too many instances defined for resource " + resourceId);
                            }
                            int numberOfDefaultInstances = defaultQuantity - instanceElements.size();
                            for (int i = 0; i < numberOfDefaultInstances; i++) {
                                String name = "#" + i;
                                DynamicResourceInstance instance = new DynamicResourceInstance(defaultCost,
                                        defaultTimeUnit);
                                resourceInstances.put(name, instance);

                                if (defaultTimetableId != null) {
                                    resourcesToTimetableIds.get(resourceId).put(name, defaultTimetableId);
                                }
                            }

                            // parse defined resource instances
                            for (Element element : instanceElements) {
                                String name = element.getAttributeValue("name");
                                if (name == null) {
                                    throw new ScyllaValidationException(
                                            "Resource instance of type " + resourceId + " does not have name.");
                                }
                                Double cost;
                                if (element.getAttributeValue("cost") == null) {
                                    cost = defaultCost;
                                }
                                else {
                                    cost = Double.valueOf(element.getAttributeValue("cost"));
                                }
                                TimeUnit timeUnit;
                                if (element.getAttributeValue("timeUnit") == null) {
                                    timeUnit = defaultTimeUnit;
                                }
                                else {
                                    timeUnit = TimeUnit.valueOf(element.getAttributeValue("timeUnit"));
                                }
                                DynamicResourceInstance instance = new DynamicResourceInstance(cost, timeUnit);
                                if (resourceInstances.containsKey(name)) {
                                    throw new ScyllaValidationException("Duplicate resource instance: " + name);
                                }
                                resourceInstances.put(name, instance);

                                String timetableId = element.getAttributeValue("timetableId");
                                if (timetableId != null) {
                                    resourcesToTimetableIds.get(resourceId).put(name, timetableId);
                                }
                            }

                            resources.put(resourceId, dynamicResource);
                        }
                        else {
                            DebugLogger.log("Element " + elem.getName()
                                    + " of resource data is expected to be known, but not supported.");
                        }
                    }
                }
                else if (elementName.equals("timetables")) {
                    List<Element> tElements = el.getChildren("timetable", bsimNamespace);
                    for (Element tElement : tElements) {
                        String timetableId = tElement.getAttributeValue("id");
                        List<TimetableItem> items = new ArrayList<TimetableItem>();
                        List<Element> tItemElements = tElement.getChildren("timetableItem", bsimNamespace);
                        for (Element tItemElement : tItemElements) {
                            DayOfWeek weekdayFrom = DayOfWeek.valueOf(tItemElement.getAttributeValue("from"));
                            DayOfWeek weekdayTo = DayOfWeek.valueOf(tItemElement.getAttributeValue("to"));
                            LocalTime beginTime = LocalTime.parse(tItemElement.getAttributeValue("beginTime"));
                            LocalTime endTime = LocalTime.parse(tItemElement.getAttributeValue("endTime"));
                            // TODO check for overlapping timetable items and handle them
                            if (DateTimeUtils.compareWeekdayTime(weekdayFrom, beginTime, weekdayTo, endTime) != 0) {
                                if (weekdayFrom.compareTo(weekdayTo) > 0) { // e.g. FRIDAY to MONDAY
                                    TimetableItem item = new TimetableItem(weekdayFrom, DayOfWeek.SUNDAY, beginTime,
                                            LocalTime.MAX);
                                    items.add(item);
                                    item = new TimetableItem(DayOfWeek.MONDAY, weekdayTo, LocalTime.MIN, endTime);
                                    items.add(item);
                                }
                                else {
                                    TimetableItem item = new TimetableItem(weekdayFrom, weekdayTo, beginTime, endTime);
                                    items.add(item);
                                }
                            }
                        }
                        timetables.put(timetableId, items);
                    }
                }
            }
            else {
                DebugLogger.log("Element " + el.getName() + " of global configuration is not supported.");
            }

        }

        // match timetables (if any available) and resource data (if any available)s
        for (String resourceId : resourcesToTimetableIds.keySet()) {
            Map<String, String> resourceInstanceIdToTimetableIds = resourcesToTimetableIds.get(resourceId);
            for (String resourceInstanceName : resourceInstanceIdToTimetableIds.keySet()) {
                String timetableId = resourceInstanceIdToTimetableIds.get(resourceInstanceName);
                if (!timetables.containsKey(timetableId)) {
                    DebugLogger.log("Timetable " + timetableId + " not found.");
                }
                List<TimetableItem> timetable = timetables.get(timetableId);
                Resource resource = resources.get(resourceId);
                if (resource instanceof DynamicResource) {
                    DynamicResource dResource = (DynamicResource) resource;
                    dResource.getResourceInstances().get(resourceInstanceName).setTimetable(timetable);
                }
            }
        }
        if (resources.isEmpty()) {
            //throw new ScyllaValidationException("No resource data definitions in file.");
        	System.err.println("[Warning:] No resource data definitions in file.");
        }
        if (randomSeed == null) {
            Random random = new Random();
            randomSeed = random.nextLong();
        }

        DebugLogger
                .log("Random seed for whole simulation (if not overriden by simulation configuration): " + randomSeed);

        GlobalConfiguration globalConfiguration = new GlobalConfiguration(globalConfId, zoneId, randomSeed, resources,
                resourceAssignmentOrder);
        return globalConfiguration;
    }

    private boolean isKnownElement(String name) {
        return name.equals("resourceAssignmentOrder") || name.equals("randomSeed") || name.equals("zoneOffset")
                || name.equals("resourceData") || name.equals("timetables");
    }

}
