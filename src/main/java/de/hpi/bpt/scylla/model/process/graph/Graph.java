package de.hpi.bpt.scylla.model.process.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;

/**
 * Graph representation of nodes.
 * 
 * @author Tsun Yin Wong
 * @param <V>
 */
public class Graph<V> {

    private Map<V, Node<V>> nodes;

    /**
     * Constructor.
     */
    public Graph() {
        nodes = new HashMap<V, Node<V>>();
    }

    public void addNode(V node) {
        nodes.put(node, new Node<V>(node));
    }

    public void addEdge(V source, V target) {
        // System.out.println(source + ", " + target);
        if (source != null) {
            Node<V> sourceNode;
            sourceNode = nodes.get(source);
            if (sourceNode == null) {
                sourceNode = new Node<V>(source);
                nodes.put(source, sourceNode);
            }
            if (target != null) {
                sourceNode.addTarget(target);
            }
        }

        if (target != null) {
            Node<V> targetNode = nodes.get(target);
            if (targetNode == null) {
                targetNode = new Node<V>(target);
                nodes.put(target, targetNode);
            }
            if (source != null) {
                targetNode.addSource(source);
            }
        }
    }

    public Map<V, Node<V>> getNodes() {
        return nodes;
    }

    public Set<V> getNodesWithoutSource() throws NodeNotFoundException {
        Set<V> startNodes = new HashSet<V>();
        for (Node<V> node : (Collection<Node<V>>) nodes.values()) {
            if (node.getSourceObjects().isEmpty()) {
                startNodes.add(node.getObject());
            }
        }
        if (startNodes.isEmpty()) {
            throw new NodeNotFoundException("No nodes without source found. Graph is cyclical.");
        }
        return startNodes;
    }

    public Set<V> getNodesWithoutTarget() throws NodeNotFoundException {
        Set<V> endNodes = new HashSet<V>();
        for (Node<V> node : (Collection<Node<V>>) nodes.values()) {
            if (node.getTargetObjects().isEmpty()) {
                endNodes.add(node.getObject());
            }
        }
        if (endNodes.isEmpty()) {
            throw new NodeNotFoundException("No nodes without target found. Graph is cyclical.");
        }
        return endNodes;
    }

    public Set<V> getSourceObjects(V obj) throws NodeNotFoundException {
        Node<V> node = nodes.get(obj);
        if (node == null) {
            throw new NodeNotFoundException("Graph node with object " + obj + " not found.");
        }
        return node.getSourceObjects();
    }

    public Set<V> getTargetObjects(V obj) throws NodeNotFoundException {
        Node<V> node = nodes.get(obj);
        if (node == null) {
            throw new NodeNotFoundException("Graph node with object " + obj + " not found.");
        }
        return node.getTargetObjects();
    }
    
    Node<V> getNode(V nId){
    	return nodes.get(nId);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (V n : nodes.keySet()) {
            Node<V> node = nodes.get(n);
            sb.append(n + "-in" + node.getSourceObjects() + "-out" + node.getTargetObjects() + "\n");
        }
        return sb.toString();
    }
    
    /*public String print() {
    	StringBuffer sb = new StringBuffer();
        for (V n : nodes.keySet()) {
        	if (!getNode(n).getNodeId().contains("DataObject")){
	            Node<V> node = nodes.get(n);
	            sb.append(getNode(n).getNodeId());
	            Set<V> nodeSourceObjects = node.getSourceObjects();
	            if (!nodeSourceObjects.isEmpty()) {
	            	sb.append(" reads from ");
	            }
	            Integer nSCounter = nodeSourceObjects.size();
	            for (V sn : nodeSourceObjects) {
	            	Node<V> sN = getNode(sn);
	            	if (sN.getNodeId().contains("DataObject")) {
	            		sb.append(sN.getNodeId());
	            		if (nSCounter != 1) {
	            			sb.append(", ");
	            		}
	            	}
	            	nSCounter--;
	            }
	            
	            Set<V> nodeTargetObjects = node.getTargetObjects();
	            if (!nodeTargetObjects.isEmpty()) {
	            	sb.append(" writes on ");
	            }
	            Integer nTCounter = nodeTargetObjects.size();
	            for (V tn : nodeTargetObjects) {
	            	Node<V> tN = getNode(tn);
	            	if (tN.getNodeId().contains("DataObject")) {
	            		sb.append(tN.getNodeId());
	            		if (nTCounter != 1) {
	            			sb.append(", ");
	            		}
	            	}  
	            	nTCounter--;
	            }
	            sb.append("\n");
        	}
        }
        return sb.toString();
    }*/
}
