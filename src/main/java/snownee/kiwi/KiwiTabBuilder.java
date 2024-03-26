package snownee.kiwi;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class KiwiTabBuilder extends CreativeModeTab.Builder {

	public static final List<KiwiTabBuilder> BUILDERS = Lists.newArrayList();

	public final ResourceLocation id;
	private CreativeModeTab tab;

	public KiwiTabBuilder(ResourceLocation id) {
		super(CreativeModeTab.Row.TOP, 0);
		this.id = id;
		title(Component.translatable("itemGroup.%s.%s".formatted(id.getNamespace(), id.getPath())));
		BUILDERS.add(this);
	}

	@Override
	public CreativeModeTab build() {
		if (tab == null) {
			tab = super.build();
		}
		return tab;
	}

}
