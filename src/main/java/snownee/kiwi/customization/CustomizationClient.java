package snownee.kiwi.customization;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.behavior.SitManager;
import snownee.kiwi.customization.block.loader.BlockDefinitionProperties;
import snownee.kiwi.customization.block.loader.KBlockDefinition;
import snownee.kiwi.customization.builder.BuildersButton;
import snownee.kiwi.customization.builder.ConvertScreen;
import snownee.kiwi.customization.builder.DebugEntryBuilderMode;
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
			buildersButtonKey = new SmartKey.Builder("key.kiwi.builders_button2", KeyMapping.Category.GAMEPLAY)
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
					.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
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
		forgeEventBus.addListener((ExtractBlockOutlineRenderStateEvent event) -> {
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

		Identifier debugEntryId = Kiwi.id("builder_mode");
		DebugScreenEntries.register(debugEntryId, new DebugEntryBuilderMode());
		List<Map.Entry<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>>> profiles = DebugScreenEntries.PROFILES.entrySet()
				.stream()
				.toList();
		var newProfiles = ImmutableMap.<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>>builder();
		for (var profile : profiles) {
			var map = ImmutableMap.<Identifier, DebugScreenEntryStatus>builder()
					.putAll(profile.getValue())
					.put(debugEntryId, DebugScreenEntryStatus.ALWAYS_ON)
					.build();
			newProfiles.put(profile.getKey(), map);
		}
		DebugScreenEntries.PROFILES = newProfiles.build();
	}

	public static void afterRegister(
			Map<Identifier, KItemDefinition> items,
			Map<Identifier, KBlockDefinition> blocks,
			ClientProxy.Context context) {
		Map<Pair<Block, Integer>, BlockTintSource> blockColors = Maps.newHashMap();
		List<Pair<Block, List<BlockTintSource>>> blocksToAdd = Lists.newArrayList();
		blocks:
		for (var entry : blocks.entrySet()) {
			BlockDefinitionProperties properties = entry.getValue().properties();
			if (properties.colorProvider().isEmpty() || properties.colorProvider().get().isEmpty()) {
				continue;
			}
			Block block = BuiltInRegistries.BLOCK.get(entry.getKey()).map($ -> $.value()).orElse(Blocks.AIR);
			int layer = 0;
			List<BlockTintSource> tintSources = Lists.newArrayList();
			for (Identifier id : properties.colorProvider().get()) {
				Block providerBlock = BuiltInRegistries.BLOCK.get(id).map($ -> $.value()).orElse(Blocks.AIR);
				if (providerBlock == Blocks.AIR) {
					Kiwi.LOGGER.warn("Cannot find color provider block %s for block %s".formatted(id, entry.getKey()));
					continue blocks;
				}

				tintSources.add(blockColors.computeIfAbsent(
						Pair.of(providerBlock, layer),
						$ -> ColorProviderUtil.delegate($.getFirst(), $.getSecond())));
				layer++;
			}
			blocksToAdd.add(Pair.of(block, tintSources));
		}
		ClientProxy.registerColors(context, blocksToAdd);
	}
}
