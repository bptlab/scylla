package de.hpi.bpt.scylla.plugin.boundaryevent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.node.EventDefinitionType;
import de.hpi.bpt.scylla.model.process.node.EventType;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.dist.DiscreteDistEmpirical;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

class BoundaryEventPluginUtils {

    static final String PLUGIN_NAME = "boundaryevent";
    private static BoundaryEventPluginUtils singleton;
    // identifier is task begin event name
    private Map<String, BoundaryObject> boundaryObjects;

    // TODO BoundaryTCPlugin and BoundaryTTPlugin to remove boundary object (replaces generateMoreBoundaryEvents
    // attribute in BoundaryObject)

    private BoundaryEventPluginUtils() {
        boundaryObjects = new HashMap<String, BoundaryObject>();
    }

    // TODO: Rethink if a singleton is the right decision here.
    static BoundaryEventPluginUtils getInstance() {
        if (singleton == null) {
            singleton = new BoundaryEventPluginUtils();
        }
        return singleton;
    }

    // This sets all necessary values for the current boundary object and stores them in the global boundary objects map.
    void initializeBoundaryObject(double beginTimeOfTask, ScyllaEvent desmojEvent, List<Integer> referenceToBoundaryEvents) {
        ProcessInstance processInstance = desmojEvent.getProcessInstance();
        int nodeId = desmojEvent.getNodeId();
        ProcessSimulationComponents desmojObjects = desmojEvent.getDesmojObjects();
        String eventName = desmojEvent.getName();
        String source = desmojEvent.getSource();

        BoundaryObject bo = new BoundaryObject(source, beginTimeOfTask, processInstance, nodeId, desmojObjects,
                referenceToBoundaryEvents);
        boundaryObjects.put(eventName, bo);
    }

    void createAndScheduleBoundaryEvents(ScyllaEvent event, TimeSpan timeSpan) throws ScyllaRuntimeException {
        double startOfInterval = event.presentTime().getTimeAsDouble(TimeUnit.SECONDS);
        double endOfInterval = startOfInterval + timeSpan.getTimeAsDouble(TimeUnit.SECONDS);

        SimulationModel model = (SimulationModel) event.getModel();

        // First create all the corresponding boundary events and then schedule them.
        if (startOfInterval < endOfInterval) {
            createBoundaryEvents(model, startOfInterval, endOfInterval, event);
            scheduleBoundaryEvents(model, startOfInterval, endOfInterval);
        }
        else { // ==
            //double nextEventTime = getNextEventTime(event); //Why would you take this, better take getTimeSpanToNextEventMap()

            double nextEventTime = startOfInterval+event.getTimeSpanToNextEventMap().get(0).getTimeAsDouble(TimeUnit.SECONDS);
            createBoundaryEvents(model, startOfInterval, nextEventTime, event);
            scheduleBoundaryEvents(model, startOfInterval, nextEventTime);
        }
//        Moved to BoundaryIntermediateEventPlugin, events determinate if the should be cancelled
//        int nodeId = event.getNodeId();
//        ProcessInstance processInstance = event.getProcessInstance();
//        ProcessModel processModel = processInstance.getProcessModel();
//        Boolean isCancelActivity = processModel.getCancelActivities().get(nodeId);
//        if (isCancelActivity != null && isCancelActivity) {
//            event.getProcessInstance().cancel();
//        }
    }

    private void createBoundaryEvents(SimulationModel model, double startOfInterval, double endOfInterval, ScyllaEvent event)
            throws ScyllaRuntimeException {

        //for (String taskBeginEventName : boundaryObjects.keySet()) {
        BoundaryObject bo = boundaryObjects.get(event.getName());

        // TODO: Remove the distinction betwenn timer and non-timer events
        // step 1
        createTimerBoundaryEvents(model, bo, startOfInterval, endOfInterval);
        // step 2
        createNonTimerBoundaryEvents(model, bo, startOfInterval, endOfInterval);
        // }
    }

    // I did not touch this for now. Hopefully could be deleted in future.
    private void createTimerBoundaryEvents(SimulationModel model, BoundaryObject bo, double startOfInterval,
                                           double endOfInterval) throws ScyllaRuntimeException {

        double beginTimeOfTask = bo.getBeginTimeOfTask();

        ProcessSimulationComponents desmojObjects = bo.getDesmojObjects();
        ProcessModel processModel = desmojObjects.getProcessModel();
        Map<Integer, EventType> eventTypes = processModel.getEventTypes();
        Map<Integer, Boolean> cancelActivities = processModel.getCancelActivities();

        List<Integer> referenceToBoundaryEvents = bo.getReferenceToBoundaryEvents();
        for (Integer nId : referenceToBoundaryEvents) {
            boolean timerEventIsInterrupting = false;
            EventType eventType = eventTypes.get(nId);
            if (eventType == EventType.BOUNDARY) {
                Map<EventDefinitionType, Map<String, String>> eventDefinitions = processModel.getEventDefinitions()
                        .get(nId);
                Map<String, String> definitionAttributes = eventDefinitions.get(EventDefinitionType.TIMER);
                if (definitionAttributes != null) { // if boundary event is timer event

                    double timeUntilWhenTimerEventsAreCreated = bo.getTimeUntilWhenTimerEventsAreCreated();

                    if (definitionAttributes.get("timeDuration") != null) {
                        String timeDuration = definitionAttributes.get("timeDuration"); // ISO 8601 duration
                        if (beginTimeOfTask != timeUntilWhenTimerEventsAreCreated) {
                            // timer event has already been created once, skip
                            continue;
                        }
                        Duration javaDuration = Duration.parse(timeDuration);
                        double duration = javaDuration.get(ChronoUnit.SECONDS);

                        if (duration == 0) {
                            continue;
                        }
                        double timeToSchedule = beginTimeOfTask + duration;
                        if (timeToSchedule < endOfInterval) {
                            String displayName = processModel.getDisplayNames().get(nId);
                            if (displayName == null) {
                                displayName = processModel.getIdentifiers().get(nId);
                            }

                            String source = bo.getSource();
                            ProcessInstance processInstance = bo.getProcessInstance();

                            TimeInstant timeInstant = new TimeInstant(startOfInterval, TimeUnit.SECONDS);

                            BPMNIntermediateEvent event = new BPMNIntermediateEvent(model, source, timeInstant,
                                    desmojObjects, processInstance, nId);

                            bo.getBoundaryEventsToSchedule().computeIfAbsent(timeToSchedule,
                                    k -> new ArrayList<BPMNIntermediateEvent>());
                            bo.getBoundaryEventsToSchedule().get(timeToSchedule).add(event);

                            String message = "Schedule boundary timer event: " + displayName;
                            bo.getMessagesOfBoundaryEventsToSchedule().computeIfAbsent(timeToSchedule, k -> new ArrayList<String>());
                            bo.getMessagesOfBoundaryEventsToSchedule().get(timeToSchedule).add(message);
                            // timeUntilWhenTimerEventsAreCreated = timeToSchedule;
                        }
                        timeUntilWhenTimerEventsAreCreated = timeToSchedule; // TODO fix boundary
                    }
                    else if (definitionAttributes.get("timeCycle") != null) {
                        String timeCycle = definitionAttributes.get("timeCycle"); // ISO 8601 repeating time interval:
                        // Rn/[ISO 8601 duration] where n
                        // (optional) for number of
                        // recurrences
                        String[] recurrencesAndDuration = timeCycle.split("/");// ["Rn"], "[ISO 8601 duration]"]
                        String recurrencesString = recurrencesAndDuration[0];
                        String timeDurationString = recurrencesAndDuration[1];
                        Integer recurrencesMax = null;
                        if (recurrencesString.length() > 1) {
                            recurrencesMax = Integer
                                    .parseInt(recurrencesString.substring(1, recurrencesString.length()));

                            timerEventIsInterrupting = cancelActivities.get(nId);

                            if (timerEventIsInterrupting) {
                                recurrencesMax = 1;
                            }
                        }

                        Duration javaDuration = Duration.parse(timeDurationString);
                        double duration = javaDuration.get(ChronoUnit.SECONDS);

                        if (duration == 0 || recurrencesMax != null && recurrencesMax == 0) {
                            continue;
                        }

                        double timeToSchedule = beginTimeOfTask;
                        int actualNumberOfOccurrences = 0;
                        boolean recurrencesMaxExceeded = false;
                        while (timeToSchedule <= timeUntilWhenTimerEventsAreCreated) {
                            timeToSchedule += duration;
                            actualNumberOfOccurrences++;
                            if (recurrencesMax != null && actualNumberOfOccurrences > recurrencesMax) {
                                recurrencesMaxExceeded = true;
                                break;
                            }
                        }
                        if (recurrencesMaxExceeded) {
                            continue;
                        }

                        while (timeToSchedule <= endOfInterval) {
                            // add as many timer events for scheduling as possible (lots of them if timer event is
                            // non-interrupting,
                            // only one if it is interrupting

                            String displayName = processModel.getDisplayNames().get(nId);
                            if (displayName == null) {
                                displayName = processModel.getIdentifiers().get(nId);
                            }

                            String source = bo.getSource();
                            ProcessInstance processInstance = bo.getProcessInstance();

                            TimeInstant timeInstant = new TimeInstant(startOfInterval, TimeUnit.SECONDS);

                            BPMNIntermediateEvent event = new BPMNIntermediateEvent(model, source, timeInstant,
                                    desmojObjects, processInstance, nId);

                            bo.getBoundaryEventsToSchedule().computeIfAbsent(timeToSchedule,
                                    k -> new ArrayList<BPMNIntermediateEvent>());
                            bo.getBoundaryEventsToSchedule().get(timeToSchedule).add(event);

                            String message = "Schedule boundary timer event: " + displayName;
                            bo.getMessagesOfBoundaryEventsToSchedule().computeIfAbsent(timeToSchedule, k -> new ArrayList<String>());
                            bo.getMessagesOfBoundaryEventsToSchedule().get(timeToSchedule).add(message);

                            actualNumberOfOccurrences++;
                            if (recurrencesMax != null && actualNumberOfOccurrences == recurrencesMax) {
                                // recurrencesMaxExceeded = true;
                                break;
                            }

                            timeToSchedule += duration;
                        }
                        timeUntilWhenTimerEventsAreCreated = timeToSchedule;
                    }
                    else { // TODO support timeDate attributes?
                        String identifier = processModel.getIdentifiers().get(nId);
                        DebugLogger.log("Timer event " + identifier + " has no timer definition, skip.");
                        continue;
                    }
                    bo.setTimeUntilWhenTimerEventsAreCreated(timeUntilWhenTimerEventsAreCreated);
                }
            }

        }
    }


    private void createNonTimerBoundaryEvents(SimulationModel model, BoundaryObject bo, double startOfInterval,
                                              double endOfInterval) throws ScyllaRuntimeException {

        double timeUntilWhenNonTimerEventsAreCreated = bo.getTimeUntilWhenNonTimerEventsAreCreated();
        if (!bo.isGenerateMoreNonTimerBoundaryEvents() || timeUntilWhenNonTimerEventsAreCreated >= endOfInterval) {
            return;
        }

        ProcessSimulationComponents desmojObjects = bo.getDesmojObjects();
        ProcessModel processModel = desmojObjects.getProcessModel();
        Map<Integer, EventType> eventTypes = processModel.getEventTypes();
        Map<Integer, Boolean> cancelActivities = processModel.getCancelActivities();

        int nodeId = bo.getNodeId();
        while (timeUntilWhenNonTimerEventsAreCreated < endOfInterval) { // If the parent task has not already ended...
            // simulation configuration defines probability of firing boundary events
            Map<Integer, Object> branchingDistributions = desmojObjects.getExtensionDistributions().get(PLUGIN_NAME);
            @SuppressWarnings("unchecked")
            DiscreteDistEmpirical<Integer> distribution = (DiscreteDistEmpirical<Integer>) branchingDistributions
                    .get(nodeId);

            if (distribution == null) { // There are no non-timer boundary events at this task...
                bo.setGenerateMoreNonTimerBoundaryEvents(false);
                return;
            }

            // decide on next node
            model.skipTraceNote();
            Integer nodeIdOfElementToSchedule = distribution.sample();
//            System.out.println("Choosed: "+processModel.getIdentifiers().get(nodeIdOfElementToSchedule)+" "+processModel.getIdentifiers().get(nodeId));
            if (nodeIdOfElementToSchedule == nodeId) {
                // No next boundary non-timer event, finish
                bo.setGenerateMoreNonTimerBoundaryEvents(false);
                return;
            }
            else { // There are boundary events
                EventType eventType = eventTypes.get(nodeIdOfElementToSchedule);
                if (eventType == EventType.BOUNDARY) {

                    // Determine whether the boundary event to schedule is an interrupting one.
                    boolean eventIsInterrupting = cancelActivities.get(nodeIdOfElementToSchedule);

                    // Get time relative to the start of the task when this boundary event will trigger.
                    double relativeTimeToTrigger = desmojObjects.getDistributionSample(nodeIdOfElementToSchedule);

                    if (relativeTimeToTrigger == 0) { // If this happens something is wrong anyways...
                        continue;
                    }

                    // Add the relative time of this boundary event, to determine when no more events are scheduled.
                    TimeUnit unit = desmojObjects.getDistributionTimeUnit(nodeIdOfElementToSchedule);
                    TimeSpan durationAsTimeSpan = new TimeSpan(relativeTimeToTrigger, unit);
                    timeUntilWhenNonTimerEventsAreCreated += durationAsTimeSpan.getTimeAsDouble(TimeUnit.SECONDS);

                    // Took this message sending part out. It was just to complicated for boundary events, fixed to their parent task.
                    // Furthermore it is not needed anymore.

                    /*String message = null;
                    boolean showInTrace = model.traceIsOn();
                    Map<EventDefinitionType, Map<String, String>> definitions = processModel.getEventDefinitions().get(nodeIdOfElementToSchedule);

                    String displayName = processModel.getDisplayNames().get(nodeIdOfElementToSchedule);

                    if (displayName == null) {
                        displayName = processModel.getIdentifiers().get(nodeIdOfElementToSchedule);
                    }
                    for (EventDefinitionType definition : definitions.keySet()) {
                        if (definition == EventDefinitionType.MESSAGE) {
                            message = "Schedule boundary message event: " + displayName;
                        }
                        else if (definition == EventDefinitionType.CONDITIONAL) {
                            message = "Schedule boundary conditional event: " + displayName;
                        }
                        else if (definition == EventDefinitionType.SIGNAL) {
                            message = "Schedule boundary signal event: " + displayName;
                        }
                        else if (definition == EventDefinitionType.ESCALATION) {
                            message = "Schedule boundary escalation event: " + displayName;
                        }
                        else {
                            if (eventIsInterrupting) {
                                if (definition == EventDefinitionType.ERROR) {
                                    message = "Schedule boundary error event: " + displayName;
                                }
                                else if (definition == EventDefinitionType.COMPENSATION) {
                                    message = "Schedule boundary compensation event: " + displayName;
                                }
                                else if (definition == EventDefinitionType.CANCEL) {
                                    message = "Schedule boundary cancel event: " + displayName;
                                }
                            }
                            else {
                                SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName,
                                        nodeIdOfElementToSchedule);
                                SimulationUtils.abort(model, processInstance, nodeId, showInTrace);
                                String identifier = processModel.getIdentifiers().get(nodeIdOfElementToSchedule);
                                throw new ScyllaRuntimeException("BPMNEvent " + identifier + " not supported.");
                            }
                        }*

                        bo.getMessagesOfBoundaryEventsToSchedule().computeIfAbsent(timeUntilWhenNonTimerEventsAreCreated,
                                k -> new ArrayList<String>());
                        bo.getMessagesOfBoundaryEventsToSchedule().get(timeUntilWhenNonTimerEventsAreCreated)
                                .add(message);
                    }
                    */

                    String source = bo.getSource();
                    ProcessInstance processInstance = bo.getProcessInstance();
                    TimeInstant timeInstant = new TimeInstant(startOfInterval, TimeUnit.SECONDS);

                    // And create the event with the time it should trigger.
                    BPMNIntermediateEvent event = new BPMNIntermediateEvent(model, source, timeInstant, desmojObjects,
                            processInstance, nodeIdOfElementToSchedule);

                    bo.getBoundaryEventsToSchedule().computeIfAbsent(timeUntilWhenNonTimerEventsAreCreated, k -> new ArrayList<BPMNIntermediateEvent>());
                    bo.getBoundaryEventsToSchedule().get(timeUntilWhenNonTimerEventsAreCreated).add(event);

                    if (eventIsInterrupting) { // If the element is interrupting, finish and clean up
                        //boundaryObjects.values().remove(bo);
                        bo.setGenerateMoreNonTimerBoundaryEvents(false);
                        break;
                    }
                }
            }
        }

        bo.setTimeUntilWhenNonTimerEventsAreCreated(timeUntilWhenNonTimerEventsAreCreated);
    }

    private void scheduleBoundaryEvents(SimulationModel model, double startOfInterval, double endOfInterval) {

        Set<String> boundaryObjectsToRemove = new HashSet<>();
        for (String taskEnableEventName : boundaryObjects.keySet()) {
            //boolean isInterruptingEvent = false;
            BoundaryObject bo = boundaryObjects.get(taskEnableEventName);

            TreeMap<Double, List<BPMNIntermediateEvent>> boundaryEventsToSchedule = bo.getBoundaryEventsToSchedule();

            Iterator<Double> iterator = boundaryEventsToSchedule.keySet().iterator();
            Set<Double> elementsToRemove = new HashSet<>();

            ProcessInstance processInstance = bo.getProcessInstance();

            while (iterator.hasNext()) {
                Double timeToSchedule = iterator.next();
                if (timeToSchedule > endOfInterval) {
                    // We will not have prepared events for scheduling which are beyond endOfInterval.
                    break;
                }
                List<BPMNIntermediateEvent> events = boundaryEventsToSchedule.get(timeToSchedule);

                // Now take all events and schedule them.
                for (BPMNIntermediateEvent event : events) {
                    double durationRelativeToEventStart = timeToSchedule - startOfInterval;
                    if (durationRelativeToEventStart < 0) continue;

                    TimeUnit unit = TimeUnit.SECONDS;
                    TimeSpan timeSpan = new TimeSpan(durationRelativeToEventStart, unit);

                    // Schedule the event.
                    try {
                        SimulationUtils.scheduleEvent(event, timeSpan);
                    } catch (ScyllaRuntimeException exception) {
                        exception.printStackTrace();
                    }

                    /*ProcessModel processModel = processInstance.getProcessModel();
                    int nodeId = event.getNodeId();
                    boolean cancelActivity = processModel.getCancelActivities().get(nodeId);
                    if (cancelActivity) {
                        isInterruptingEvent = true;
                    }*/
                }


                // TreeMap<Double, List<String>> messagesOfBoundaryEventsToSchedule = bo.getMessagesOfBoundaryEventsToSchedule();
                // Took this out, see rest in createNonTimerBoundaryEvents.
                /*List<String> messages = messagesOfBoundaryEventsToSchedule.get(timeToSchedule);
                for (String message : messages) {
                    model.sendTraceNote(message);
                }*/

                // clean up
                elementsToRemove.add(timeToSchedule);

                // Not needed anymore, alreday done in creation.
                /*if (isInterruptingEvent) {
                    boundaryObjectsToRemove.add(taskEnableEventName);
                    // if (bo.getSource().equals(desmojEvent.getSource())) {
                    // normalBehavior = false;
                    // }
                    // processInstance.cancel();
                    break;
                }*/
            }

            for (Double timeToSchedule : elementsToRemove) {
                boundaryEventsToSchedule.remove(timeToSchedule);
                // messagesOfBoundaryEventsToSchedule.remove(timeToSchedule);
                if (boundaryEventsToSchedule.isEmpty()) {
                    boundaryObjectsToRemove.add(taskEnableEventName);
                }
            }
        }

        // Delete all boudnaryObjects, which are compeltetly scheduled.
        for (String taskEnableEventName : boundaryObjectsToRemove) {
            boundaryObjects.remove(taskEnableEventName);
        }
    }


    // not needed anymore, is covered by getTimeToNextEventMap()
    /*private double getNextEventTime(ScyllaEvent event) {
        double nextEventTime = Double.MAX_VALUE;
        List<Entity> entities = event.getModel().getEntities(false);
        for (Entity entity : entities) {
            TimeInstant timeInstant = entity.scheduledNext();
            if (timeInstant != null) {
                double nextEventTimeOfEntity = timeInstant.getTimeAsDouble(TimeUnit.SECONDS);
                if (nextEventTimeOfEntity < nextEventTime) {
                    nextEventTime = nextEventTimeOfEntity;
                }
            }
        }
        return nextEventTime;
    }*/
}
