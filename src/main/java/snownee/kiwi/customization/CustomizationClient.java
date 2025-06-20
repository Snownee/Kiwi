package snownee.kiwi.customization;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.NeoForge;
import snownee.kiwi.Kiwi;
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
		List<Pair<Block, BlockColor>> blocksToAdd = Lists.newArrayList();
		for (var entry : blocks.entrySet()) {
			BlockDefinitionProperties properties = entry.getValue().properties();
			if (context.loading()) {
				RenderLayerEnum renderType = properties.renderType().orElse(null);
				if (renderType == null) {
					renderType = properties.glassType().map(GlassType::renderType).orElse(null);
				}
				if (renderType != null) {
					Block block = BuiltInRegistries.BLOCK.getValue(entry.getKey());
					ItemBlockRenderTypes.setRenderLayer(block, (ChunkSectionLayer) renderType.value);
				}
			}
			if (properties.colorProvider().isEmpty()) {
				continue;
			}
			Block block = BuiltInRegistries.BLOCK.getValue(entry.getKey());
			ResourceLocation colorProvider = properties.colorProvider().get();
			// grass -> short_grass since Minecraft 1.20.3
			if (ResourceLocation.DEFAULT_NAMESPACE.equals(colorProvider.getNamespace()) && colorProvider.getPath().equals("grass")) {
				colorProvider = ResourceLocation.withDefaultNamespace("short_grass");
			}
			Block providerBlock = BuiltInRegistries.BLOCK.getValue(colorProvider);
			if (providerBlock == Blocks.AIR) {
				Kiwi.LOGGER.warn("Cannot find color provider block %s for block %s".formatted(colorProvider, entry.getKey()));
			} else {
				blocksToAdd.add(Pair.of(block, blockColors.computeIfAbsent(providerBlock, ColorProviderUtil::delegate)));
			}
		}
		ClientProxy.registerColors(context, blocksToAdd);
	}
}
