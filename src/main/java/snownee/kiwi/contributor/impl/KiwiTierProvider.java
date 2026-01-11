package snownee.kiwi.contributor.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class KiwiTierProvider extends JsonTierProvider {
	private final List<String> renderableTiers = List.of("2020q3", "2020q4"/*, "2021q1"*/, "sunny_milk", "xmas");

	public KiwiTierProvider() {
		super("Snownee", KiwiTierProvider::getURLs);
	}

	private static List<String> getURLs() {
		String cdn = "https://cdn.jsdelivr.net/gh/Snownee/Kiwi@master/contributors.json";
		String github = "https://raw.githubusercontent.com/Snownee/Kiwi/master/contributors.json";
		return List.of(cdn, github);
	}

	private static boolean isInXmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) >= 15;
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
		return Set.copyOf(getRenderableTiers());
	}

	@Override
	public List<String> getRenderableTiers() {
		return renderableTiers;
	}

}
