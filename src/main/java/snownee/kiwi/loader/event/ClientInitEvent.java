package snownee.kiwi.loader.event;

import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;

public class ClientInitEvent extends ParallelEvent {

	public ClientInitEvent(ParallelDispatchEvent delegate) {
		super(delegate);
	}

}
