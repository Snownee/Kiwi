package snownee.kiwi.customization;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.kiwi.customization.block.behavior.SitManager;
import snownee.kiwi.customization.builder.BuildersButton;
import snownee.kiwi.customization.builder.ConvertScreen;
import snownee.kiwi.customization.command.ExportBlocksCommand;
import snownee.kiwi.customization.command.ExportCreativeTabsCommand;
import snownee.kiwi.customization.command.ExportShapesCommand;
import snownee.kiwi.customization.command.ReloadBlockSettingsCommand;
import snownee.kiwi.customization.command.ReloadFamiliesAndRulesCommand;
import snownee.kiwi.customization.command.ReloadSlotsCommand;
import snownee.kiwi.util.SmartKey;

public final class CustomizationClient {
	public static final SmartKey buildersButtonKey = new SmartKey.Builder("key.kiwi.builders_button", KeyMapping.CATEGORY_GAMEPLAY)
			.key(InputConstants.getKey("key.mouse.4"))
			.onLongPress(BuildersButton::onLongPress)
			.onShortPress(BuildersButton::onShortPress)
			.build();

	public static void init() {
		var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		var forgeEventBus = MinecraftForge.EVENT_BUS;
		modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
			event.register(buildersButtonKey);
		});
		forgeEventBus.addListener((TickEvent.ClientTickEvent event) -> {
			if (event.phase == TickEvent.Phase.END) {
				buildersButtonKey.tick();
				ConvertScreen.tickLingering();
			}
		});
		forgeEventBus.addListener((ScreenEvent.MouseButtonPressed.Pre event) -> {
			if (buildersButtonKey.matchesMouse(event.getButton()) && buildersButtonKey.setDownWithResult(true)) {
				event.setCanceled(true);
			}
		});
		forgeEventBus.addListener((ScreenEvent.MouseButtonReleased.Pre event) -> {
			if (buildersButtonKey.matchesMouse(event.getButton()) && buildersButtonKey.setDownWithResult(false)) {
				event.setCanceled(true);
			}
		});
		forgeEventBus.addListener((ScreenEvent.KeyPressed.Pre event) -> {
			if (buildersButtonKey.matches(event.getKeyCode(), event.getScanCode()) && buildersButtonKey.setDownWithResult(true)) {
				event.setCanceled(true);
			}
		});
		forgeEventBus.addListener((ScreenEvent.KeyReleased.Pre event) -> {
			if (buildersButtonKey.matches(event.getKeyCode(), event.getScanCode()) && buildersButtonKey.setDownWithResult(false)) {
				event.setCanceled(true);
			}
		});
		forgeEventBus.addListener((RegisterCommandsEvent event) -> {
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
		forgeEventBus.addListener((TickEvent.RenderTickEvent event) -> {
			if (event.phase != TickEvent.Phase.START) {
				return;
			}
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null && SitManager.isSeatEntity(player.getVehicle())) {
				SitManager.clampRotation(player, player.getVehicle());
			}
		});
	}

	public static void pushScreen(Minecraft mc, Screen screen) {
		mc.pushGuiLayer(screen);
	}

	@Nullable
	public static Slot getSlotUnderMouse(AbstractContainerScreen<?> containerScreen) {
		return containerScreen.getSlotUnderMouse();
	}
}
