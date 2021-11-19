package snownee.kiwi.loader.event;

import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;

public class PostInitEvent extends ParallelEvent {

	public PostInitEvent(ParallelDispatchEvent delegate) {
		super(delegate);
	}

}
