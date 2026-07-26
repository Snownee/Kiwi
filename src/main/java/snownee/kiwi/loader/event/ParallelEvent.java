package snownee.kiwi.loader.event;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;
import snownee.kiwi.Kiwi;

public class ParallelEvent {
	ParallelDispatchEvent delegate;

	public ParallelEvent(ParallelDispatchEvent delegate) {
		this.delegate = delegate;
	}

	public CompletableFuture<Void> enqueueWork(Runnable work) {
		return delegate.enqueueWork(work).exceptionally(throwable -> {
			Kiwi.LOGGER.error("Error while executing work in parallel event", throwable);
			throw new RuntimeException(throwable);
		});
	}

	public <T> CompletableFuture<T> enqueueWork(Supplier<T> work) {
		return delegate.enqueueWork(work).exceptionally(throwable -> {
			Kiwi.LOGGER.error("Error while executing work in parallel event", throwable);
			throw new RuntimeException(throwable);
		});
	}
}
