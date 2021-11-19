package snownee.kiwi.loader.event;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;

public class ParallelEvent {
	ParallelDispatchEvent delegate;

	public ParallelEvent(ParallelDispatchEvent delegate) {
		this.delegate = delegate;
	}

	public CompletableFuture<Void> enqueueWork(Runnable work) {
		return delegate.enqueueWork(work);
	}

	public <T> CompletableFuture<T> enqueueWork(Supplier<T> work) {
		return delegate.enqueueWork(work);
	}
}
