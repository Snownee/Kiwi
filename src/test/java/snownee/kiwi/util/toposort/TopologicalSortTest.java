package snownee.kiwi.util.toposort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

class TopologicalSortTest {
	@Test
	void sortsPrerequisitesFirst() {
		MutableGraph<String> graph = GraphBuilder.directed().build();
		graph.putEdge("a", "c");
		graph.putEdge("b", "c");

		List<String> sorted = TopologicalSort.topologicalSort(graph, Comparator.naturalOrder());

		assertEquals(List.of("a", "b", "c"), sorted);
	}

	@Test
	void comparatorOrdersReadyNodes() {
		MutableGraph<String> graph = GraphBuilder.directed().build();
		graph.putEdge("a", "c");
		graph.addNode("b");

		List<String> sorted = TopologicalSort.topologicalSort(graph, Comparator.reverseOrder());

		assertEquals(List.of("b", "a", "c"), sorted);
	}

	@Test
	void reportsCycles() {
		MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(true).build();
		graph.putEdge("a", "b");
		graph.putEdge("b", "a");
		graph.putEdge("c", "c");

		CyclePresentException exception =
				assertThrows(CyclePresentException.class, () -> TopologicalSort.topologicalSort(graph, null));

		Set<Set<String>> cycles = exception.getCycles();
		assertEquals(2, cycles.size());
		assertTrue(cycles.contains(Set.of("a", "b")));
		assertTrue(cycles.contains(Set.of("c")));
	}

	@Test
	void stronglyConnectedComponents() {
		MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(true).build();
		graph.putEdge("a", "b");
		graph.putEdge("b", "a");
		graph.putEdge("b", "c");
		graph.addNode("d");

		Set<Set<String>> components = new StronglyConnectedComponentDetector<>(graph).getComponents();

		assertEquals(Set.of(Set.of("a", "b"), Set.of("c"), Set.of("d")), components);
	}
}
