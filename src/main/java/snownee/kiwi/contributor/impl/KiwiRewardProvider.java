package snownee.kiwi.contributor.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.client.RewardLayer;
import snownee.kiwi.contributor.impl.client.layer.ElectronicatLayer;
import snownee.kiwi.contributor.impl.client.layer.FoxTailLayer;
import snownee.kiwi.contributor.impl.client.layer.PlanetLayer;
import snownee.kiwi.contributor.impl.client.layer.SantaHatLayer;
import snownee.kiwi.contributor.impl.client.layer.SunnyMilkLayer;

public class KiwiRewardProvider extends JsonRewardProvider {
    public KiwiRewardProvider() {
        super("Snownee", KiwiRewardProvider::getURLs);
    }

    private static List<String> getURLs() {
        String cdn = "https://cdn.jsdelivr.net/gh/Snownee/Kiwi@master/contributors.json";
        String github = "https://raw.githubusercontent.com/Snownee/Kiwi/master/contributors.json";
        String coding = "https://snownee.coding.net/p/test/d/test/git/raw/master/contributors.json";
        Locale locale = Locale.getDefault();
        if (locale.getCountry().equals("CN") && Calendar.getInstance().get(Calendar.ZONE_OFFSET) == 28800000) {
            Kiwi.logger.debug("Use fetching strategy 1");
            return ImmutableList.of(cdn, coding);
        } else {
            Kiwi.logger.debug("Use fetching strategy 2");
            return ImmutableList.of(cdn, github);
        }
    }

    private final List<String> renderableTiers = ImmutableList.of("2020q3", "2020q4"/*, "2021q1"*/, "sunny_milk");

    private static boolean isInXmas() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) >= 20;
    }

    @Override
    public Set<String> getPlayerTiers(String playerName) {
        Set<String> ret = super.getPlayerTiers(playerName);
        if (isInXmas()) {
            ret = Sets.newHashSet(ret);
            ret.add("xmas");
        }
        return ret;
    }

    @Override
    public Set<String> getTiers() {
        return ImmutableSet.copyOf(getRenderableTiers());
    }

    @Override
    public List<String> getRenderableTiers() {
        return renderableTiers;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RewardLayer createRenderer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer, String tier) {
        switch (tier) {
        case "2020q3":
            return new PlanetLayer(entityRenderer);
        case "2020q4":
            return new FoxTailLayer(entityRenderer);
//        case "2021q1":
//            return new ElectronicatLayer(entityRenderer);
        case "xmas":
            return new SantaHatLayer(entityRenderer);
        case "sunny_milk":
            return new SunnyMilkLayer(entityRenderer);
        default:
            return null;
        }
    }

}
