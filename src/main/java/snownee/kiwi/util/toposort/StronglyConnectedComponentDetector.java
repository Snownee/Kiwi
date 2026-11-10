package snownee.kiwi.util.toposort;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.graph.Graph;

/**
 * Splits a directed graph into strongly connected components.
 *
 * <p>Uses Tarjan's algorithm. Every node belongs to exactly one component; a component with more
 * than one node, or a single node with a self-loop, represents a cycle.
 */
public class StronglyConnectedComponentDetector<T> {
	private final Graph<T> graph;

	public StronglyConnectedComponentDetector(Graph<T> graph) {
		this.graph = graph;
	}

	/**
	 * Returns all strongly connected components of the graph.
	 *
	 * @return one set per component
	 */
	public Set<Set<T>> getComponents() {
		Map<T, Integer> indices = new HashMap<>();
		Map<T, Integer> lowLinks = new HashMap<>();
		Deque<T> stack = new ArrayDeque<>();
		Set<T> onStack = new HashSet<>();
		Set<Set<T>> components = new LinkedHashSet<>();
		int[] nextIndex = {0};
		for (T node : graph.nodes()) {
			if (!indices.containsKey(node)) {
				visit(node, indices, lowLinks, stack, onStack, components, nextIndex);
			}
		}
		return components;
	}

	private void visit(
			T node,
			Map<T, Integer> indices,
			Map<T, Integer> lowLinks,
			Deque<T> stack,
			Set<T> onStack,
			Set<Set<T>> components,
			int[] nextIndex) {
		int index = nextIndex[0]++;
		indices.put(node, index);
		lowLinks.put(node, index);
		stack.push(node);
		onStack.add(node);

		for (T successor : graph.successors(node)) {
			if (!indices.containsKey(successor)) {
				visit(successor, indices, lowLinks, stack, onStack, components, nextIndex);
				lowLinks.put(node, Math.min(lowLinks.get(node), lowLinks.get(successor)));
			} else if (onStack.contains(successor)) {
				lowLinks.put(node, Math.min(lowLinks.get(node), indices.get(successor)));
			}
		}

		if (Objects.equals(lowLinks.get(node), indices.get(node))) {
			Set<T> component = new LinkedHashSet<>();
			T member;
			do {
				member = stack.pop();
				onStack.remove(member);
				component.add(member);
			} while (!Objects.equals(member, node));
			components.add(component);
		}
	}
}
