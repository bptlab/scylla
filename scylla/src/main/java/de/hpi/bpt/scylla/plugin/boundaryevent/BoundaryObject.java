package de.hpi.bpt.scylla.plugin.boundaryevent;

import java.util.List;
import java.util.TreeMap;

import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;

class BoundaryObject {

    private String source;

    private ProcessSimulationComponents pSimComponents;
    private ProcessInstance processInstance;
    private int nodeId;

    private double beginTimeOfTask;
    private double timerEventsCreatedUntil; // inclusive
    private double nonTimerEventsCreatedUntil; // inclusive
    private List<Integer> referenceToBoundaryEvents;
    private boolean generateMoreBoundaryEvents = true;

    // ordered by Double
    private TreeMap<Double, List<BPMNIntermediateEvent>> boundaryEventsToSchedule = new TreeMap<Double, List<BPMNIntermediateEvent>>();
    // for scheduling boundary events
    private TreeMap<Double, List<String>> messagesOfBoundaryEventsToSchedule = new TreeMap<Double, List<String>>();

    BoundaryObject(String source, double enableTimeOfTask, ProcessInstance processInstance, int nodeId,
            ProcessSimulationComponents desmojObjects, List<Integer> referenceToBoundaryEvents) {
        this.source = source;
        this.beginTimeOfTask = enableTimeOfTask;
        this.timerEventsCreatedUntil = enableTimeOfTask;
        this.nonTimerEventsCreatedUntil = enableTimeOfTask;
        this.processInstance = processInstance;
        this.nodeId = nodeId;
        this.pSimComponents = desmojObjects;
        this.referenceToBoundaryEvents = referenceToBoundaryEvents;
    }

    String getSource() {
        return source;
    }

    ProcessSimulationComponents getDesmojObjects() {
        return pSimComponents;
    }

    ProcessInstance getProcessInstance() {
        return processInstance;
    }

    int getNodeId() {
        return nodeId;
    }

    double getBeginTimeOfTask() {
        return beginTimeOfTask;
    }

    double getTimeUntilWhenTimerEventsAreCreated() {
        return timerEventsCreatedUntil;
    }

    void setTimeUntilWhenTimerEventsAreCreated(double timeUntilWhenTimerEventsAreCreated) {
        this.timerEventsCreatedUntil = timeUntilWhenTimerEventsAreCreated;
    }

    double getTimeUntilWhenNonTimerEventsAreCreated() {
        return nonTimerEventsCreatedUntil;
    }

    void setTimeUntilWhenNonTimerEventsAreCreated(double timeUntilWhenNonTimerEventsAreCreated) {
        this.nonTimerEventsCreatedUntil = timeUntilWhenNonTimerEventsAreCreated;
    }

    List<Integer> getReferenceToBoundaryEvents() {
        return referenceToBoundaryEvents;
    }

    boolean isGenerateMoreNonTimerBoundaryEvents() {
        return generateMoreBoundaryEvents;
    }

    void setGenerateMoreNonTimerBoundaryEvents(boolean generateMoreNonTimerBoundaryEvents) {
        this.generateMoreBoundaryEvents = generateMoreNonTimerBoundaryEvents;
    }

    TreeMap<Double, List<BPMNIntermediateEvent>> getBoundaryEventsToSchedule() {
        return boundaryEventsToSchedule;
    }

    TreeMap<Double, List<String>> getMessagesOfBoundaryEventsToSchedule() {
        return messagesOfBoundaryEventsToSchedule;
    }

}
