package de.hpi.bpt.scylla.model.process.graph;

import java.util.Set;
import java.util.TreeSet;

/**
 * Generic node structure with references to predecessors and successors.
 * 
 * @author Tsun Yin Wong
 *
 * @param <V>
 */
public class Node<V> {

    private V object;
    private Set<V> sourceObjects;
    private Set<V> targetObjects;
    //private String id;

    Node(V obj) {
        object = obj;
        sourceObjects = new TreeSet<V>();
        targetObjects = new TreeSet<V>();
    }

    void addSource(V obj) {
        sourceObjects.add(obj);
    }
    
    /*public void setId(String identifier) {
    	this.id = identifier;
    }
    
    public String getNodeId() {
    	return this.id;
    }*/
    
    void addTarget(V obj) {
        targetObjects.add(obj);
    }

    public V getObject() {
        return object;
    }

    Set<V> getSourceObjects() {
        return sourceObjects;
    }

    Set<V> getTargetObjects() {
        return targetObjects;
    }

}
