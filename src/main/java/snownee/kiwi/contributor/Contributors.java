package snownee.kiwi.contributor;

import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.client.RewardLayer;
import snownee.kiwi.contributor.client.gui.RewardScreen;
import snownee.kiwi.contributor.impl.KiwiRewardProvider;
import snownee.kiwi.contributor.network.CSetEffectPacket;
import snownee.kiwi.contributor.network.SSyncEffectPacket;
import snownee.kiwi.network.NetworkChannel;
import snownee.kiwi.util.Util;

@KiwiModule("contributors")
@KiwiModule.Subscriber
public class Contributors extends AbstractModule {

    public static final Map<String, ITierProvider> REWARD_PROVIDERS = Maps.newConcurrentMap();
    public static final Map<String, ResourceLocation> PLAYER_EFFECTS = Maps.newConcurrentMap();
    private static final Set<ResourceLocation> RENDERABLES = Sets.newLinkedHashSet();
    private static int DAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    @Override
    protected void preInit() {
        NetworkChannel.register(CSetEffectPacket.class, new CSetEffectPacket.Handler());
        NetworkChannel.register(SSyncEffectPacket.class, new SSyncEffectPacket.Handler());
    }

    @Override
    protected void init(FMLCommonSetupEvent event) {
        registerTierProvider(new KiwiRewardProvider());
    }

    public static boolean isContributor(String author, String playerName) {
        return REWARD_PROVIDERS.getOrDefault(author.toLowerCase(Locale.ENGLISH), ITierProvider.Empty.INSTANCE).isContributor(playerName);
    }

    public static boolean isContributor(String author, String playerName, String tier) {
        return REWARD_PROVIDERS.getOrDefault(author.toLowerCase(Locale.ENGLISH), ITierProvider.Empty.INSTANCE).isContributor(playerName, tier);
    }

    public static boolean isContributor(String author, PlayerEntity player) {
        return isContributor(author, player.getGameProfile().getName());
    }

    public static boolean isContributor(String author, PlayerEntity player, String tier) {
        return isContributor(author, player.getGameProfile().getName(), tier);
    }

    public static Set<ResourceLocation> getPlayerTiers(String playerName) {
        /* off */
        return REWARD_PROVIDERS.values().stream()
                .flatMap(tp -> tp.getPlayerTiers(playerName).stream()
                        .map(s -> new ResourceLocation(tp.getAuthor().toLowerCase(Locale.ENGLISH), s)))
                .collect(Collectors.toSet());
        /* on */
    }

    public static Set<ResourceLocation> getTiers() {
        /* off */
        return REWARD_PROVIDERS.values().stream()
                .flatMap(tp -> tp.getTiers().stream()
                        .map(s -> new ResourceLocation(tp.getAuthor().toLowerCase(Locale.ENGLISH), s)))
                .collect(Collectors.toSet());
        /* on */
    }

    public static void registerTierProvider(ITierProvider rewardProvider) {
        String namespace = rewardProvider.getAuthor().toLowerCase(Locale.ENGLISH);
        REWARD_PROVIDERS.put(namespace, rewardProvider);
        for (String tier : rewardProvider.getRenderableTiers()) {
            RENDERABLES.add(new ResourceLocation(namespace, tier));
        }
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        new SSyncEffectPacket(PLAYER_EFFECTS).send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent event) {
        changeEffect();
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PLAYER_EFFECTS.remove(event.getPlayer().getGameProfile().getName());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        PLAYER_EFFECTS.clear();
        RewardLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidateAll());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void clientInit(FMLClientSetupEvent event) {
        Minecraft.getInstance().getRenderManager().getSkinMap().values().forEach(renderer -> {
            RewardLayer layer = new RewardLayer(renderer);
            RewardLayer.ALL_LAYERS.add(layer);
            renderer.addLayer(layer);
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void changeEffect() {
        ResourceLocation id = Util.RL(KiwiClientConfig.contributorEffect);
        if (id != null && id.getPath().isEmpty()) {
            id = null;
        }
        if (!canPlayerUseEffect(getPlayerName(), id)) {
            ConfigHandler cfg = KiwiConfigManager.getHandler(KiwiClientConfig.class);
            ConfigValue<String> val = (ConfigValue<String>) cfg.getValueByPath("contributorEffect");
            val.set("");
            cfg.refresh();
            return;
        }
        new CSetEffectPacket(id).send();
        if (id == null) {
            PLAYER_EFFECTS.remove(getPlayerName());
        } else {
            PLAYER_EFFECTS.put(getPlayerName(), id);
            Kiwi.logger.info("Enabled contributor effect: {}", id);
        }
        RewardLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidate(getPlayerName()));
    }

    @OnlyIn(Dist.CLIENT)
    public static void changeEffect(Map<String, ResourceLocation> changes) {
        changes.forEach((k, v) -> {
            if (v == null) {
                PLAYER_EFFECTS.remove(k);
            } else {
                PLAYER_EFFECTS.put(k, v);
            }
        });
        RewardLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidateAll(changes.keySet()));
    }

    public static void changeEffect(ServerPlayerEntity player, ResourceLocation effect) {
        String playerName = player.getGameProfile().getName();
        if (!canPlayerUseEffect(playerName, effect)) {
            return;
        }
        if (effect == null) {
            PLAYER_EFFECTS.remove(playerName);
        } else {
            PLAYER_EFFECTS.put(playerName, effect);
        }
        new SSyncEffectPacket(ImmutableMap.of(playerName, effect)).sendExcept(player);
    }

    public static boolean isRenderable(ResourceLocation id) {
        refreshRenderables();
        return RENDERABLES.contains(id);
    }

    public static Set<ResourceLocation> getRenderableTiers() {
        refreshRenderables();
        return Collections.unmodifiableSet(RENDERABLES);
    }

    private static void refreshRenderables() {
        int current = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (current != DAY) {
            DAY = current;
            RENDERABLES.clear();
            for (Entry<String, ITierProvider> entry : REWARD_PROVIDERS.entrySet()) {
                String namespace = entry.getKey();
                for (String tier : entry.getValue().getRenderableTiers()) {
                    RENDERABLES.add(new ResourceLocation(namespace, tier));
                }
            }
        }
    }

    public static boolean canPlayerUseEffect(String playerName, ResourceLocation effect) {
        if (effect == null || effect.getPath().isEmpty()) {
            return true;
        }
        if (!isContributor(effect.getNamespace(), playerName, effect.getPath())) {
            return false;
        }
        return isRenderable(effect);
    }

    @OnlyIn(Dist.CLIENT)
    private static String getPlayerName() {
        return Minecraft.getInstance().getSession().getUsername();
    }

    private int hold;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onKeyInput(KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.currentScreen != null || mc.player == null || !mc.isGameFocused()) {
            return;
        }
        if (event.getModifiers() != 0) {
            return;
        }
        Input input = InputMappings.getInputByCode(event.getKey(), event.getScanCode());
        if (input.getKeyCode() != 75) {
            return;
        }
        if (event.getAction() != 2) {
            hold = 0;
        } else if (++hold == 30) {
            RewardScreen screen = new RewardScreen();
            mc.displayGuiScreen(screen);
        }
    }

}
