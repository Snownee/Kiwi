package snownee.kiwi.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;

public class KiwiTabBuilder extends CreativeModeTab.Builder {

	public final Identifier id;

	public KiwiTabBuilder(Identifier id) {
		super(CreativeModeTab.Row.TOP, 0);
		this.id = id;
		title(Component.translatable("itemGroup.%s.%s".formatted(id.getNamespace(), id.getPath())));
	}

}
