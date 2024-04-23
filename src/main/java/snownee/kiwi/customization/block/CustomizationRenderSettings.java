package snownee.kiwi.customization.block;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.customization.block.loader.BlockDefinitionProperties;
import snownee.kiwi.customization.block.loader.KBlockDefinition;
import snownee.kiwi.util.ClientProxy;
import snownee.kiwi.util.ColorProviderUtil;

public interface CustomizationRenderSettings {
	static void init(Map<ResourceLocation, KBlockDefinition> blocks, ClientProxy.Context context) {
		Map<Block, BlockColor> blockColors = Maps.newHashMap();
		Map<Block, ItemColor> itemColors = Maps.newHashMap();
		List<Pair<Block, BlockColor>> blocksToAdd = Lists.newArrayList();
		List<Pair<Item, ItemColor>> itemsToAdd = Lists.newArrayList();
		for (var entry : blocks.entrySet()) {
			BlockDefinitionProperties properties = entry.getValue().properties();
			if (context.loading()) {
				KiwiModule.RenderLayer.Layer renderType = properties.renderType().orElse(null);
				if (renderType == null) {
					renderType = properties.glassType().map(GlassType::renderType).orElse(null);
				}
				if (renderType != null) {
					Block block = BuiltInRegistries.BLOCK.get(entry.getKey());
					ItemBlockRenderTypes.setRenderLayer(block, (RenderType) renderType.value);
				}
			}
			if (properties.colorProvider().isPresent()) {
				Block block = BuiltInRegistries.BLOCK.get(entry.getKey());
				Block providerBlock = BuiltInRegistries.BLOCK.get(properties.colorProvider().get());
				if (providerBlock == Blocks.AIR) {
					Kiwi.LOGGER.warn("Cannot find color provider block %s for block %s".formatted(
							properties.colorProvider().get(),
							entry.getKey()));
				} else {
					blocksToAdd.add(Pair.of(block, blockColors.computeIfAbsent(providerBlock, ColorProviderUtil::delegate)));
				}
				Item item = block.asItem();
				Item providerItem = providerBlock.asItem();
				if (item != Items.AIR) {
					if (providerItem != Items.AIR) {
						itemsToAdd.add(Pair.of(
								item,
								itemColors.computeIfAbsent(providerBlock, $ -> ColorProviderUtil.delegate($.asItem()))));
					} else if (providerBlock == Blocks.WATER) {
						itemsToAdd.add(Pair.of(item, (stack, i) -> 0x3f76e4));
					} else {
						itemsToAdd.add(Pair.of(item, itemColors.computeIfAbsent(providerBlock, ColorProviderUtil::delegateItemFallback)));
					}
				}
			}
		}
		ClientProxy.registerColors(context, blocksToAdd, itemsToAdd);
	}
}
