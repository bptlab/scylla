package de.hpi.bpt.scylla.plugin_loader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyGraph <T> {
	
	public static class CycleException extends Exception {
		private static final long serialVersionUID = 3122267406745368493L;
		
		public CycleException() {
			this("Could not resolve dependencies due to cyclic reference");
		}
		public CycleException(String message) {
			super(message);
		}
	}
	
	private class Node{
		private T value;
		private Set<Node> edges;
		private Node(T forValue) {
			value = forValue;
			edges = new HashSet<>();
			nodes.add(this);
			valueMap.put(value, this);
		}
		private void addEdge(Node to) {
			edges.add(to);
		}
		
		private void clean() {
			visited = finished = false;
		}
		private boolean visited, finished;
		
		private void visit() throws CycleException {
			visited = true;
			for(Node n : edges) {
				if(n.finished)continue;
				if(n.visited)throw new CycleException();
				n.visit();
			}
			finish();
		}
		private void finish() {
			finished = true;
			resolved.add(0, value);
		}
	}
	
	private List<Node> nodes;
	private Map<T,Node> valueMap;
	
	private List<T> resolved;
	
	public DependencyGraph(List<T> values){
		nodes = new LinkedList<>();
		valueMap = new HashMap<>();
		values.stream().forEach(Node::new);
	}
	
	public void createEdge(T from, T to) {
		getNode(from).addEdge(getNode(to));
	}
	
	private Node getNode(T value) {
		return valueMap.get(value);
	}
	
	public List<T> resolve() throws CycleException {
		for(Node n : nodes)n.clean();
		resolved = new LinkedList<T>();
		
		//Reverse iteration, because it is more stable
		//E.g. a list without dependencies will simply stay as is and not be completely reversed as it would when iterating forward
		for(int i = nodes.size()-1; i >= 0; i--) {
			Node n = nodes.get(i);
			if(!n.visited)n.visit();
		}
		
		return resolved;
	}

}
