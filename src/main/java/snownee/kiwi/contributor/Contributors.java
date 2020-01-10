package snownee.kiwi.contributor;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiConfig;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.contributor.client.RewardLayer;
import snownee.kiwi.contributor.impl.KiwiRewardProvider;
import snownee.kiwi.contributor.network.CSetEffectPacket;
import snownee.kiwi.contributor.network.SSyncEffectPacket;
import snownee.kiwi.network.NetworkChannel;

@KiwiModule(name = "contributors")
@KiwiModule.Subscriber
public class Contributors extends AbstractModule {

    public static final Map<String, IRewardProvider> REWARD_PROVIDERS = Maps.newConcurrentMap();
    public static final Map<String, ResourceLocation> PLAYER_EFFECTS = Maps.newConcurrentMap();

    @Override
    protected void preInit() {
        NetworkChannel.register(CSetEffectPacket.class, new CSetEffectPacket.Handler());
        NetworkChannel.register(SSyncEffectPacket.class, new SSyncEffectPacket.Handler());
    }

    @Override
    protected void init(FMLCommonSetupEvent event) {
        registerRewardProvider(new KiwiRewardProvider());
    }

    public static boolean isContributor(String author, String playerName) {
        return REWARD_PROVIDERS.getOrDefault(author.toLowerCase(Locale.ENGLISH), IRewardProvider.Empty.INSTANCE).isContributor(playerName);
    }

    public static boolean isContributor(String author, String playerName, String tier) {
        return REWARD_PROVIDERS.getOrDefault(author.toLowerCase(Locale.ENGLISH), IRewardProvider.Empty.INSTANCE).isContributor(playerName, tier);
    }

    public static boolean isContributor(String author, PlayerEntity player) {
        return isContributor(author, player.getGameProfile().getName());
    }

    public static boolean isContributor(String author, PlayerEntity player, String tier) {
        return isContributor(author, player.getGameProfile().getName(), tier);
    }

    public static Set<ResourceLocation> getRewards(String playerName) {
        /* off */
        return REWARD_PROVIDERS.values().stream()
                .flatMap(rp -> rp.getRewards(playerName).stream()
                        .map(s -> new ResourceLocation(rp.getAuthor().toLowerCase(Locale.ENGLISH), s)))
                .collect(Collectors.toSet());
        /* on */
    }

    public static void registerRewardProvider(IRewardProvider rewardProvider) {
        REWARD_PROVIDERS.put(rewardProvider.getAuthor().toLowerCase(Locale.ENGLISH), rewardProvider);
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
        if (!canPlayerUseEffect(getUserName(), KiwiConfig.contributorEffect)) {
            return;
        }
        new CSetEffectPacket(KiwiConfig.contributorEffect).send();
        if (KiwiConfig.contributorEffect == null) {
            PLAYER_EFFECTS.remove(getUserName());
        } else {
            RewardLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidate(getUserName()));
            PLAYER_EFFECTS.put(getUserName(), KiwiConfig.contributorEffect);
            Kiwi.logger.info("Enabled contributor effect: {}", KiwiConfig.contributorEffect);
        }
        RewardLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidate(getUserName()));
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

    public static boolean isValidEffect(ResourceLocation id) {
        return REWARD_PROVIDERS.containsKey(id.getNamespace()) && REWARD_PROVIDERS.get(id.getNamespace()).hasRenderer(id.getPath());
    }

    public static boolean canPlayerUseEffect(String playerName, ResourceLocation effect) {
        if (effect == null) {
            return true;
        }
        if (!isContributor(effect.getNamespace(), playerName, effect.getPath())) {
            return false;
        }
        return isValidEffect(effect);
    }

    @OnlyIn(Dist.CLIENT)
    private static String getUserName() {
        return Minecraft.getInstance().getSession().getUsername();
    }
}
