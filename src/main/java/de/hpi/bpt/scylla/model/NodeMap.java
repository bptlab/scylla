package de.hpi.bpt.scylla.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Maps from node ids (integer from 0 to numNodes-1) to objects.
 * Optimization measure, uses array access which is slightly faster than HashMap hash access.
 * Only supports most basic operations (put and get). 
 * Allows faster iteration by exposing internal array.
 * @author Leon Bein
 *
 * @param <T>
 */
public class NodeMap<T> implements Map<Integer, T> {
	
	public final Object[] data;
	
	public NodeMap(int numNodes) {
		data = new Object[numNodes];
	}


	@Override
	public boolean containsKey(Object key) {
		if (key instanceof Integer) {
			Integer nodeId = (Integer) key;
			return nodeId >= 0 && nodeId < size();
		} else {
			return false;
		}
	}

	@Override
	public T get(Object key) {
		if (key instanceof Integer) {
			int nodeId = (Integer) key;
			return get(nodeId);
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public T get(int nodeId) {
		return (T) data[nodeId];
	}

	@Override
	public T put(Integer nodeId, T value) {
		T oldValue = get(nodeId);
		data[nodeId] = value;
		return oldValue;
	}

	@Override
	public T remove(Object key) {
		if (key instanceof Integer) {
			Integer nodeId = (Integer) key;
			return put(nodeId, null);
		} else {
			return null;
		}
	}
	

	@Override
	public int size() {
		throw new UnsupportedOperationException();
		//return data.size();
	}
	
	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends T> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Integer> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<T> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<Integer, T>> entrySet() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

}
