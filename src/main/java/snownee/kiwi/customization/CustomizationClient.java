package snownee.kiwi.customization;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ChunkSectionLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.Kiwi;
import snownee.kiwi.RenderLayerEnum;
import snownee.kiwi.customization.block.GlassType;
import snownee.kiwi.customization.block.behavior.SitManager;
import snownee.kiwi.customization.block.loader.BlockDefinitionProperties;
import snownee.kiwi.customization.block.loader.KBlockDefinition;
import snownee.kiwi.customization.builder.BuildersButton;
import snownee.kiwi.customization.builder.ConvertScreen;
import snownee.kiwi.customization.command.ExportBlocksCommand;
import snownee.kiwi.customization.command.ExportCreativeTabsCommand;
import snownee.kiwi.customization.command.ExportMappingsCommand;
import snownee.kiwi.customization.command.ExportShapesCommand;
import snownee.kiwi.customization.command.PrintFamiliesCommand;
import snownee.kiwi.customization.command.ReloadBlockSettingsCommand;
import snownee.kiwi.customization.command.ReloadFamiliesAndRulesCommand;
import snownee.kiwi.customization.command.ReloadSlotsCommand;
import snownee.kiwi.customization.item.loader.KItemDefinition;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.ClientProxy;
import snownee.kiwi.util.client.ColorProviderUtil;
import snownee.kiwi.util.client.SmartKey;

public final class CustomizationClient {
	@Nullable
	public static SmartKey buildersButtonKey;

	public static void init() {
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			ConvertScreen.tickLingering();
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
			LiteralArgumentBuilder<CommandSourceStack> kiwi = Commands.literal("kiwi");
			LiteralArgumentBuilder<CommandSourceStack> customization = Commands.literal("customization").requires(Commands.hasPermission(
					Commands.LEVEL_GAMEMASTERS));
			LiteralArgumentBuilder<CommandSourceStack> export = Commands.literal("export");
			ExportBlocksCommand.register(export);
			ExportShapesCommand.register(export);
			ExportCreativeTabsCommand.register(export);
			LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload");
			ReloadSlotsCommand.register(reload);
			ReloadBlockSettingsCommand.register(reload);
			ReloadFamiliesAndRulesCommand.register(reload);
			PrintFamiliesCommand.register(customization);
			if (!Platform.isProduction()) {
				ExportMappingsCommand.register(export);
			}
			dispatcher.register(kiwi.then(customization.then(export).then(reload)));
		});
//		forgeEventBus.addListener((CustomizeGuiOverlayEvent.DebugText event) -> {
//			BuildersButton.renderDebugText(event.getLeft(), event.getRight());
//		});
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((_, _) -> {
			return !BuildersButton.cancelRenderHighlight();
		});
		LevelRenderEvents.START_MAIN.register(context -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null && SitManager.isSeatEntity(player.getVehicle())) {
				SitManager.clampRotation(player, player.getVehicle());
			}
		});
	}

	public static void afterRegister(
			Map<Identifier, KItemDefinition> items,
			Map<Identifier, KBlockDefinition> blocks,
			ClientProxy.Context context) {
		buildersButtonKey = new SmartKey.Builder("key.kiwi.builders_button2", KeyMapping.Category.GAMEPLAY)
				.onLongPress(BuildersButton::onLongPress)
				.onShortPress(BuildersButton::onShortPress)
				.build();
		KeyMappingHelper.registerKeyMapping(buildersButtonKey);
		ClientProxy.afterRegisterSmartKey(buildersButtonKey);
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
					ChunkSectionLayerMap.putBlock(block, (ChunkSectionLayer) renderType.value);
				}
			}
			if (properties.colorProvider().isEmpty()) {
				continue;
			}
			Block block = BuiltInRegistries.BLOCK.getValue(entry.getKey());
			Block providerBlock = BuiltInRegistries.BLOCK.getValue(properties.colorProvider().get());
			if (providerBlock == Blocks.AIR) {
				Kiwi.LOGGER.warn("Cannot find color provider block %s for block %s".formatted(
						properties.colorProvider().get(),
						entry.getKey()));
			} else {
				blocksToAdd.add(Pair.of(block, blockColors.computeIfAbsent(providerBlock, ColorProviderUtil::delegate)));
			}
		}
		ClientProxy.registerColors(context, blocksToAdd);
	}
}
