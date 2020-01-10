package snownee.kiwi.contributor.impl;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.contributor.client.KiwiTestLayer;
import snownee.kiwi.contributor.client.RewardLayer;

public class KiwiRewardProvider extends JsonRewardProvider {

    public KiwiRewardProvider() {
        super("Snownee", "https://raw.githubusercontent.com/Snownee/Kiwi/master/contributors.json");
    }

    @Override
    public boolean hasRenderer(String tier) {
        return "2020q1".equals(tier);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RewardLayer createRenderer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer, String tier) {
        return new KiwiTestLayer(entityRenderer);
    }

}
