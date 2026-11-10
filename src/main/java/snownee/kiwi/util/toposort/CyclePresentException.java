package snownee.kiwi.util.toposort;

import java.util.Set;

/**
 * Thrown when a topological sort cannot be completed because the graph contains a cycle.
 */
public final class CyclePresentException extends IllegalArgumentException {
	private final Set<Set<?>> cycles;

	CyclePresentException(Set<Set<?>> cycles) {
		super("Cycle detected: " + cycles);
		this.cycles = cycles;
	}

	/**
	 * Returns the strongly connected components that form cycles.
	 *
	 * @param <T> node type
	 * @return one entry per cycle
	 */
	@SuppressWarnings("unchecked")
	public <T> Set<Set<T>> getCycles() {
		return (Set<Set<T>>) (Set<?>) cycles;
	}
}
