package snownee.kiwi.contributor.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.layer.FoxTailLayer;
import snownee.kiwi.contributor.impl.client.layer.PlanetLayer;
import snownee.kiwi.contributor.impl.client.layer.SantaHatLayer;
import snownee.kiwi.contributor.impl.client.layer.SunnyMilkLayer;
import snownee.kiwi.loader.Platform;

public class KiwiTierProvider extends JsonTierProvider {
	public KiwiTierProvider() {
		super("Snownee", KiwiTierProvider::getURLs);
	}

	private static List<String> getURLs() {
		String cdn = "https://cdn.jsdelivr.net/gh/Snownee/Kiwi@master/contributors.json";
		String github = "https://raw.githubusercontent.com/Snownee/Kiwi/master/contributors.json";
		Locale locale = Locale.getDefault();
		if ("CN".equals(locale.getCountry()) && Calendar.getInstance().get(Calendar.ZONE_OFFSET) == 28800000) {
			return ImmutableList.of(cdn, github);
		} else {
			return ImmutableList.of(cdn, github);
		}
	}

	private final List<String> renderableTiers = ImmutableList.of("2020q3", "2020q4"/*, "2021q1"*/, "sunny_milk", "xmas");

	private static boolean isInXmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) >= 15;
	}

	@Override
	public Set<String> getPlayerTiers(String playerName) {
		Set<String> ret = super.getPlayerTiers(Platform.isProduction() ? playerName : "Dev");
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
	@Environment(EnvType.CLIENT)
	public CosmeticLayer createRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> entityRenderer, String tier) {
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
