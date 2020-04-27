package de.hpi.bpt.scylla.simulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.parser.EventOrderType;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;

/**
 * Queue for DesmoJ events which are waiting for resource instances.
 * 
 * @author Tsun Yin Wong
 */
public class ScyllaEventQueue extends PriorityQueue<ScyllaEvent> {

    /**
     * TODO towards performance: replace PriorityQueue with SortedList (since we use it as such and never call poll() /
     * offer(e))
     */

    private static final long serialVersionUID = 886409943987052123L;

    /**
     * Constructor.
     * 
     * @param resourceId
     *            name of resource type
     * @param resourceAssignmentOrder
     *            order of events in queue
     */
    public ScyllaEventQueue(String resourceId, List<EventOrderType> resourceAssignmentOrder) {
        // sort by priority, ascending
        super(new Comparator<ScyllaEvent>() {
            @Override
            public int compare(ScyllaEvent e1, ScyllaEvent e2) {
                try {
                    for (EventOrderType eventOrder : resourceAssignmentOrder) {
                        int comp = eventOrder.compare(resourceId, e1, e2);
                        if (comp != 0) {
                            return comp;
                        }
                    }
                    // default behavior: compare times at what events were scheduled
                    return e1.getSimulationTimeOfSource().compareTo(e2.getSimulationTimeOfSource());
                }
                catch (ScyllaRuntimeException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    SimulationUtils.abort(e1.getModel(), e1.getProcessInstance(), e1.getNodeId(), e1.traceIsOn());
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Returns the event at the given index in the queue.
     * 
     * @param index
     *            the index of event in the queue
     * @return the event at the given index in the queue
     */
    public ScyllaEvent peek(int index) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        ScyllaEvent event = null;
        int size = size();
        List<ScyllaEvent> events = new ArrayList<ScyllaEvent>();
        for (int i = 0; i < size; i++) {
            if (i == index) {
                event = peek();
                break;
            }
            events.add(poll());
        }
        addAll(events);
        return event;
    }

    /**
     * Returns the index of the event in the queue.
     * 
     * @param event
     *            the event in question
     * @return the index of the event in the event queue
     */
    public int getIndex(ScyllaEvent event) {
        int index = -1;
        int size = size();
        List<ScyllaEvent> events = new ArrayList<ScyllaEvent>();
        for (int i = 0; i < size; i++) {
            if (peek().equals(event)) {
                index = i;
                break;
            }
            events.add(poll());
        }
        addAll(events);
        return index;
    }
}
