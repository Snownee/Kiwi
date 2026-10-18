package kiwi.qa;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import snownee.kiwi.config.ClothConfigIntegration;

/** Test-only entrypoint: validate mixin application and render configuration screens. */
public final class KiwiClientChecks implements ClientModInitializer {
    private final List<String> namespaces = new ArrayList<>(List.of("kiwi"));
    private int ticks;
    private int index = -1;
    private int worldTicks;
    private final boolean joinWorld = Boolean.getBoolean("kiwi.qa.world");

    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("snowrealmagic")) namespaces.add("snowrealmagic");
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.gui.overlay() != null) return;
            if (index < 0) {
                if (joinWorld) {
                    if (mc.level == null || mc.player == null) return;
                    mc.player.setYRot(180);
                    mc.player.setXRot(15);
                    if (++worldTicks == 100) net.minecraft.client.Screenshot.grab(mc, false);
                    if (worldTicks < 120) return;
                    int snowyBlocks = 0;
                    for (BlockPos pos : BlockPos.betweenClosed(-8, 118, -8, 8, 124, 8)) {
                        if (BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).getNamespace().equals("snowrealmagic")
                                && mc.level.getBlockEntity(pos) != null) snowyBlocks++;
                    }
                    if (snowyBlocks < 4) throw new AssertionError("KIWI_CLIENT_QA_FAIL expected four synchronized snowy block entities, got " + snowyBlocks);
                    System.out.println("KIWI_CLIENT_QA_WORLD_PASS " + snowyBlocks + " snowy blocks, 120 world ticks");
                } else if (!(mc.gui.screen() instanceof TitleScreen)) return;
                // Apply mixins for inventory/rendering classes without requiring an existing world.
                for (String name : List.of(
                        "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen",
                        "net.minecraft.client.gui.screens.inventory.InventoryScreen",
                        "net.minecraft.client.gui.screens.inventory.EffectsInInventory",
                        "net.minecraft.client.renderer.entity.EntityRenderDispatcher",
                        "net.minecraft.client.renderer.fog.environment.LavaFogEnvironment",
                        "net.minecraft.client.renderer.ScreenEffectRenderer",
                        "net.minecraft.client.Camera",
                        "net.minecraft.world.level.block.SnowLayerBlock",
                        "net.minecraft.client.renderer.block.dispatch.Variant",
                        "net.minecraft.client.renderer.GameRenderer",
                        "net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher")) {
                    try { Class.forName(name); }
                    catch (ClassNotFoundException e) { throw new AssertionError("KIWI_CLIENT_QA_FAIL " + name, e); }
                }
                index = 0;
            }
            if (ticks == 0) {
                var screen = ClothConfigIntegration.create(mc.gui.screen() == null ? new TitleScreen() : mc.gui.screen(), namespaces.get(index));
                if (screen == null) throw new AssertionError("KIWI_CLIENT_QA_FAIL missing config " + namespaces.get(index));
                mc.gui.setScreen(screen);
                System.out.println("KIWI_CLIENT_QA_CONFIG_OPEN " + namespaces.get(index));
            }
            if (++ticks == 40) {
                System.out.println("KIWI_CLIENT_QA_CONFIG_PASS " + namespaces.get(index));
                ticks = 0;
                if (++index == namespaces.size()) {
                    System.out.println("KIWI_CLIENT_QA_PASS");
                    mc.stop();
                }
            }
        });
    }
}
