package snownee.kiwi.loader.event;

import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;

public class InitEvent extends ParallelEvent {

	public InitEvent(ParallelDispatchEvent delegate) {
		super(delegate);
	}

}
