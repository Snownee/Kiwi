package snownee.kiwi.customization.compat.jade;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.placement.PlaceSlot;
import snownee.kiwi.loader.Platform;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		if (!Platform.isProduction()) {
			Kiwi.LOGGER.info("Registering debug Jade plugin");
			registration.registerBlockComponent(new DebugProvider(), Block.class);
		}
	}

	public static class DebugProvider implements IBlockComponentProvider {
		public static final ResourceLocation ID = Kiwi.id("debug_placement_system");

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			var side = accessor.getSide();
			List<List<String>> slots = PlaceSlot.find(accessor.getBlockState(), side).stream().map(PlaceSlot::tagList).toList();
			if (slots.isEmpty()) {
				return;
			}
			for (List<String> slot : slots) {
				tooltip.add(Component.literal(String.join(", ", slot)));
			}
		}

		@Override
		public ResourceLocation getUid() {
			return ID;
		}

		@Override
		public boolean isRequired() {
			return true;
		}
	}
}
