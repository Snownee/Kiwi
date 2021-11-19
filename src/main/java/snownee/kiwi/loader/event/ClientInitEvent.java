package snownee.kiwi.loader.event;

import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;

public class ClientInitEvent extends ParallelEvent {

	public ClientInitEvent(ParallelDispatchEvent delegate) {
		super(delegate);
	}

}
