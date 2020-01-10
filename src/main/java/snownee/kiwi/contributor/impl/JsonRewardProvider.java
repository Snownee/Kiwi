package snownee.kiwi.contributor.impl;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.IRewardProvider;
import snownee.kiwi.contributor.client.RewardLayer;

public class JsonRewardProvider implements IRewardProvider {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping()/*.registerTypeAdapter(type, typeAdapter)*/.create();
    private final String author;
    private ImmutableSetMultimap<String, String> contributors = ImmutableSetMultimap.of();

    public JsonRewardProvider(String author, String url) {
        this.author = author;
        Thread thread = new Thread(() -> {
            int tried = 0;
            while (++tried <= 3) {
                if (load(url))
                    break;
            }
        }, String.format("[Kiwi > %s] Loading Contributors", author));
        thread.setDaemon(true);
        thread.start();
    }

    public boolean load(String url) {
        try {
            InputStreamReader reader = new InputStreamReader(new URL(url).openStream());
            Map<String, Collection<String>> map = GSON.fromJson(reader, Map.class);
            final Collection<String> superUsers = map.containsKey("*") ? map.get("*") : Collections.singleton(getAuthor());
            superUsers.add("Dev");
            Builder<String, String> builder = ImmutableSetMultimap.builder();
            map.forEach((reward, users) -> {
                if (reward.equals("*")) {
                    return;
                }
                superUsers.forEach(user -> builder.put(user, reward));
                users.forEach(user -> builder.put(user, reward));
            });
            contributors = builder.build();
            return true;
        } catch (Exception e) {
            Kiwi.logger.catching(e);
            return false;
        }
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public boolean isContributor(String playerName) {
        return contributors.containsKey(playerName);
    }

    @Override
    public boolean isContributor(String playerName, String tier) {
        return contributors.containsKey(playerName) && contributors.get(playerName).contains(tier);
    }

    @Override
    public Set<String> getRewards(String playerName) {
        return contributors.containsKey(playerName) ? contributors.get(playerName) : Collections.EMPTY_SET;
    }

    @Override
    public boolean hasRenderer(String tier) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RewardLayer createRenderer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer, String tier) {
        return null;
    }

}
