package snownee.kiwi.loader.event;

import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;

public class PostInitEvent extends ParallelEvent {

	public PostInitEvent(ParallelDispatchEvent delegate) {
		super(delegate);
	}

}
