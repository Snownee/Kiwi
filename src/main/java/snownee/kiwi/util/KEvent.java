package snownee.kiwi.util;

import java.util.function.Function;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.Identifier;

public class KEvent<T> {
	public static final Identifier DEFAULT_PHASE = Event.DEFAULT_PHASE;
	protected final Event<T> wrapped;

	public KEvent(Event<T> wrapped) {
		this.wrapped = wrapped;
	}

	public static <T> KEvent<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
		return new KEvent<>(EventFactory.createArrayBacked(type, invokerFactory));
	}

	public static <T> KEvent<T> createArrayBacked(Class<T> type, T emptyInvoker, Function<T[], T> invokerFactory) {
		return new KEvent<>(EventFactory.createArrayBacked(type, emptyInvoker, invokerFactory));
	}

	public static <T> KEvent<T> createWithPhases(
			Class<? super T> type,
			Function<T[], T> invokerFactory,
			Identifier... defaultPhases) {
		return new KEvent<>(EventFactory.createWithPhases(type, invokerFactory, defaultPhases));
	}

	public final T invoker() {
		return wrapped.invoker();
	}

	public void register(T listener) {
		wrapped.register(listener);
	}

	public void register(Identifier phase, T listener) {
		wrapped.register(phase, listener);
	}

	public void addPhaseOrdering(Identifier firstPhase, Identifier secondPhase) {
		wrapped.addPhaseOrdering(firstPhase, secondPhase);
	}
}