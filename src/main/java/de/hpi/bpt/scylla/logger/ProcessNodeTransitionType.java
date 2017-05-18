package de.hpi.bpt.scylla.logger;

/**
 * Node transition types for tasks and events.
 * 
 * @author Tsun Yin Wong
 *
 */
public enum ProcessNodeTransitionType {
    ENABLE, BEGIN, PAUSE, RESUME, TERMINATE, CANCEL, EVENT_BEGIN, EVENT_TERMINATE
}
