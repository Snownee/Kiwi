package snownee.kiwi.customization;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.neoforged.bus.api.IEventBus;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.NeoForge;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.RenderLayerEnum;
import snownee.kiwi.customization.block.GlassType;
import snownee.kiwi.customization.block.behavior.SitManager;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.loader.BlockDefinitionProperties;
import snownee.kiwi.customization.block.loader.KBlockDefinition;
import snownee.kiwi.customization.builder.BuilderRules;
import snownee.kiwi.customization.builder.BuildersButton;
import snownee.kiwi.customization.builder.ConvertScreen;
import snownee.kiwi.customization.command.ExportBlocksCommand;
import snownee.kiwi.customization.command.ExportCreativeTabsCommand;
import snownee.kiwi.customization.command.ExportShapesCommand;
import snownee.kiwi.customization.command.PrintFamiliesCommand;
import snownee.kiwi.customization.command.ReloadBlockSettingsCommand;
import snownee.kiwi.customization.command.ReloadFamiliesAndRulesCommand;
import snownee.kiwi.customization.command.ReloadSlotsCommand;
import snownee.kiwi.customization.item.loader.KItemDefinition;
import snownee.kiwi.util.ClientProxy;
import snownee.kiwi.util.client.ColorProviderUtil;
import snownee.kiwi.util.client.SmartKey;

public final class CustomizationClient {
	@Nullable
	public static SmartKey buildersButtonKey;

	private static boolean enabled;

	public static void init(IEventBus modEventBus) {
		var forgeEventBus = NeoForge.EVENT_BUS;
		modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
			if (!CustomizationHooks.kswitch && BlockFamilies.all().isEmpty() && BuilderRules.all().isEmpty()) {
				return;
			}
			buildersButtonKey = new SmartKey.Builder("key.kiwi.builders_button", KeyMapping.CATEGORY_GAMEPLAY)
					.key(InputConstants.getKey("key.mouse.4"))
					.onLongPress(BuildersButton::onLongPress)
					.onShortPress(BuildersButton::onShortPress)
					.build();
			event.register(buildersButtonKey);
			ClientProxy.afterRegisterSmartKey(buildersButtonKey);
		});
		forgeEventBus.addListener((ClientTickEvent.Post event) -> ConvertScreen.tickLingering());
		forgeEventBus.addListener((RegisterClientCommandsEvent event) -> {
			LiteralArgumentBuilder<CommandSourceStack> kiwi = Commands.literal("kiwi");
			LiteralArgumentBuilder<CommandSourceStack> customization = Commands.literal("customization")
					.requires(source -> source.hasPermission(2));
			LiteralArgumentBuilder<CommandSourceStack> export = Commands.literal("export");
			ExportBlocksCommand.register(export);
			ExportShapesCommand.register(export);
			ExportCreativeTabsCommand.register(export);
			LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload");
			ReloadSlotsCommand.register(reload);
			ReloadBlockSettingsCommand.register(reload);
			ReloadFamiliesAndRulesCommand.register(reload);
			PrintFamiliesCommand.register(customization);
			event.getDispatcher().register(kiwi.then(customization.then(export).then(reload)));
		});
		forgeEventBus.addListener((CustomizeGuiOverlayEvent.DebugText event) -> {
			BuildersButton.renderDebugText(event.getLeft(), event.getRight());
		});
		forgeEventBus.addListener((RenderHighlightEvent.Block event) -> {
			if (BuildersButton.cancelRenderHighlight()) {
				event.setCanceled(true);
			}
		});
		forgeEventBus.addListener((RenderFrameEvent.Pre event) -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null && SitManager.isSeatEntity(player.getVehicle())) {
				SitManager.clampRotation(player, player.getVehicle());
			}
		});
	}

	public static void afterRegister(
			Map<ResourceLocation, KItemDefinition> items,
			Map<ResourceLocation, KBlockDefinition> blocks,
			ClientProxy.Context context) {
		Map<Block, BlockColor> blockColors = Maps.newHashMap();
		Map<Item, ItemColor> itemColors = Maps.newHashMap();
		List<Pair<Block, BlockColor>> blocksToAdd = Lists.newArrayList();
		List<Pair<Item, ItemColor>> itemsToAdd = Lists.newArrayList();
		Set<Item> addedItems = Sets.newHashSet();
		for (var entry : items.entrySet()) {
			KItemDefinition definition = entry.getValue();
			if (definition.properties().colorProvider().isEmpty()) {
				continue;
			}
			Item item = BuiltInRegistries.ITEM.get(entry.getKey());
			Item providerItem = BuiltInRegistries.ITEM.get(definition.properties().colorProvider().get());
			if (providerItem == Items.AIR) {
				Kiwi.LOGGER.warn("Cannot find color provider item %s for item %s".formatted(
						definition.properties().colorProvider().get(),
						entry.getKey()));
				continue;
			}
			itemsToAdd.add(Pair.of(item, itemColors.computeIfAbsent(providerItem, ColorProviderUtil::delegate)));
			addedItems.add(item);
		}
		for (var entry : blocks.entrySet()) {
			BlockDefinitionProperties properties = entry.getValue().properties();
			if (context.loading()) {
				RenderLayerEnum renderType = properties.renderType().orElse(null);
				if (renderType == null) {
					renderType = properties.glassType().map(GlassType::renderType).orElse(null);
				}
				if (renderType != null) {
					Block block = BuiltInRegistries.BLOCK.get(entry.getKey());
					ItemBlockRenderTypes.setRenderLayer(block, (RenderType) renderType.value);
				}
			}
			if (properties.colorProvider().isEmpty()) {
				continue;
			}
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
			if (item == Items.AIR) {
				continue;
			}
			if (addedItems.contains(item)) {
				continue;
			}
			addedItems.add(item); //sometimes multiple blocks share the same item
			Item providerItem = providerBlock.asItem();
			if (providerItem != Items.AIR) {
				itemsToAdd.add(Pair.of(
						item,
						itemColors.computeIfAbsent(providerItem, ColorProviderUtil::delegate)));
			} else if (providerBlock == Blocks.WATER) {
				itemsToAdd.add(Pair.of(item, (stack, i) -> 0x3f76e4));
			} else {
				itemsToAdd.add(Pair.of(
						item,
						itemColors.computeIfAbsent(providerItem, $ -> ColorProviderUtil.delegateItemFallback(providerBlock))));
			}
		}
		ClientProxy.registerColors(context, blocksToAdd, itemsToAdd);
	}
}
