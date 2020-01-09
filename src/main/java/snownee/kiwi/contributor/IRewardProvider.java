package snownee.kiwi.contributor;

import java.util.Collections;
import java.util.Set;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.contributor.client.RewardLayer;

public interface IRewardProvider {
    String getAuthor();

    boolean hasRenderer(String tier);

    @OnlyIn(Dist.CLIENT)
    RewardLayer createRenderer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer, String tier);

    boolean isContributor(String playerName);

    boolean isContributor(String playerName, String tier);

    Set<String> getRewards(String playerName);

    public static enum Empty implements IRewardProvider {
        INSTANCE;

        @Override
        public String getAuthor() {
            return "";
        }

        @Override
        public boolean isContributor(String playerName) {
            return false;
        }

        @Override
        public boolean isContributor(String playerName, String tier) {
            return false;
        }

        @Override
        public Set<String> getRewards(String playerName) {
            return Collections.EMPTY_SET;
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
}
