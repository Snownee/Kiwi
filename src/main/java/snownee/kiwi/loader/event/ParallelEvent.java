package snownee.kiwi.loader.event;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ParallelEvent {

	public CompletableFuture<Void> enqueueWork(Runnable work) {
		work.run();
		return CompletableFuture.completedFuture(null);
	}

	public <T> CompletableFuture<T> enqueueWork(Supplier<T> work) {
		return CompletableFuture.completedFuture(work.get());
	}
}
