package snownee.kiwi.util.toposort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.graph.Graph;

/**
 * Topological sorting for directed acyclic graphs.
 *
 * <p>Cycles are reported through {@link CyclePresentException}, with one entry per strongly
 * connected component that forms a cycle.
 */
public final class TopologicalSort {
	private TopologicalSort() {
	}

	/**
	 * Sorts the graph so that every node appears after all of its prerequisites.
	 *
	 * @param graph      graph to sort
	 * @param comparator optional ordering for nodes with equal priority
	 * @param <T>        node type
	 * @return nodes in topological order
	 * @throws CyclePresentException if the graph contains a cycle
	 */
	public static <T> List<T> topologicalSort(
			Graph<T> graph,
			@Nullable Comparator<? super T> comparator) {
		if (!graph.isDirected()) {
			throw new IllegalArgumentException("Topological sort requires a directed graph");
		}
		checkForCycles(graph);

		Map<T, Integer> remainingPrerequisites = new HashMap<>();
		for (T node : graph.nodes()) {
			remainingPrerequisites.put(node, graph.inDegree(node));
		}

		Queue<T> ready = comparator == null ? new ArrayDeque<>() : new PriorityQueue<>(comparator);
		for (Map.Entry<T, Integer> entry : remainingPrerequisites.entrySet()) {
			if (entry.getValue() == 0) {
				ready.add(entry.getKey());
			}
		}

		List<T> sorted = new ArrayList<>(graph.nodes().size());
		while (!ready.isEmpty()) {
			T node = ready.remove();
			sorted.add(node);
			for (T successor : graph.successors(node)) {
				int prerequisiteCount = remainingPrerequisites.merge(successor, -1, Integer::sum);
				if (prerequisiteCount == 0) {
					ready.add(successor);
				}
			}
		}

		if (sorted.size() != graph.nodes().size()) {
			throw new IllegalStateException("Topological sort did not visit every node");
		}
		return List.copyOf(sorted);
	}

	private static <T> void checkForCycles(Graph<T> graph) {
		Set<Set<?>> cycles = new LinkedHashSet<>();
		for (Set<T> component : new StronglyConnectedComponentDetector<>(graph).getComponents()) {
			if (component.size() > 1) {
				cycles.add(component);
				continue;
			}
			T node = component.iterator().next();
			if (graph.successors(node).contains(node)) {
				cycles.add(component);
			}
		}
		if (!cycles.isEmpty()) {
			throw new CyclePresentException(cycles);
		}
	}
}
