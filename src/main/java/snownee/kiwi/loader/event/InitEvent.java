package snownee.kiwi.loader.event;

import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;

public class InitEvent extends ParallelEvent {

	public InitEvent(ParallelDispatchEvent delegate) {
		super(delegate);
	}

}
