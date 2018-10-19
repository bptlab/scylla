package de.hpi.bpt.scylla.test.plugin_loader;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hpi.bpt.scylla.plugin_loader.DependencyGraph;
import de.hpi.bpt.scylla.plugin_loader.DependencyGraph.CycleException;

public class DependencyGraphTests {
	
	private DependencyGraph<Integer> graph;
	
	@BeforeEach
	public void setUp() {
		List<Integer> data = Arrays.asList(1,2,3,4,5,6,7,8);
		Collections.shuffle(data);
		graph = new DependencyGraph<>(data);
		graph.createEdge(1, 2);
		graph.createEdge(2, 3);
		graph.createEdge(2, 4);
		graph.createEdge(3, 5);
		graph.createEdge(4, 6);
		graph.createEdge(5, 6);
		graph.createEdge(7, 8);
	}
	
	@Test
	public void testCorrectOrder() {
		try {
			List<Integer> sortedData = graph.resolve();
			assertBeforeAll(sortedData, 1, 2,3,4,5,6);
			assertBeforeAll(sortedData, 2, 3,4,5,6);
			assertBeforeAll(sortedData, 3, 5,6);
			assertBeforeAll(sortedData, 4, 6);
			assertBeforeAll(sortedData, 5, 6);
			assertBeforeAll(sortedData, 7, 8);
		} catch (CycleException e) {
			fail(e.getMessage());
		}
	}
	
	public void assertBeforeAll(List<Integer> sortedList, Integer a, Integer... bs) {
		for(Integer b : bs)assertTrue(sortedList.indexOf(a) < sortedList.indexOf(b));
	}
	
	@Test
	public void testCycleDetected() throws CycleException {
		graph.createEdge(6, 2);
		assertThrows(CycleException.class, () -> graph.resolve());
	}

}
