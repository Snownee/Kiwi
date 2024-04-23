package snownee.kiwi.customization.placement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import snownee.kiwi.util.codec.CustomizationCodecs;
import snownee.kiwi.customization.block.KBlockUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.Kiwi;
import snownee.kiwi.loader.Platform;

public record SlotLink(
		String from,
		String to,
		int interest,
		List<TagTest> testTag,
		ResultAction onLinkFrom,
		ResultAction onLinkTo,
		ResultAction onUnlinkFrom,
		ResultAction onUnlinkTo) {
	public static final Codec<String> PRIMARY_TAG_CODEC = ExtraCodecs.validate(Codec.STRING, s -> {
		if (s.startsWith("*")) {
			return DataResult.success(s);
		}
		return DataResult.error(() -> "Primary tag must start with *");
	});
	public static final Codec<SlotLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PRIMARY_TAG_CODEC.fieldOf("from").forGetter(SlotLink::from),
			PRIMARY_TAG_CODEC.fieldOf("to").forGetter(SlotLink::to),
			Codec.INT.optionalFieldOf("interest", 100).forGetter(SlotLink::interest),
			CustomizationCodecs.strictOptionalField(TagTest.CODEC.listOf(), "test_tag", List.of()).forGetter(SlotLink::testTag),
			fromToPairCodec("on_link").forGetter($ -> Pair.of($.onLinkFrom, $.onLinkTo)),
			fromToPairCodec("on_unlink").forGetter($ -> Pair.of($.onUnlinkFrom, $.onUnlinkTo))
	).apply(instance, SlotLink::create));

	private static MapCodec<Pair<ResultAction, ResultAction>> fromToPairCodec(String fieldName) {
		Codec<Pair<ResultAction, ResultAction>> pairCodec = RecordCodecBuilder.create(instance -> instance.group(
				CustomizationCodecs.strictOptionalField(ResultAction.CODEC, "from", ResultAction.EMPTY).forGetter(Pair::getFirst),
				CustomizationCodecs.strictOptionalField(ResultAction.CODEC, "to", ResultAction.EMPTY).forGetter(Pair::getSecond)
		).apply(instance, Pair::of));
		return pairCodec.optionalFieldOf(fieldName, Pair.of(ResultAction.EMPTY, ResultAction.EMPTY));
	}

	public record Preparation(Map<ResourceLocation, SlotLink> slotLinks, PlaceSlotProvider.Preparation slotProviders) {
		public static Preparation of(
				Supplier<Map<ResourceLocation, SlotLink>> slotLinksSupplier,
				PlaceSlotProvider.Preparation slotProviders) {
			Map<ResourceLocation, SlotLink> slotLinks = Platform.isDataGen() ? Map.of() : slotLinksSupplier.get();
			return new Preparation(slotLinks, slotProviders);
		}

		public void finish() {
			SlotLink.renewData(this);
		}
	}

	private static void renewData(Preparation preparation) {
		Map<Pair<String, String>, SlotLink> map = Maps.newHashMapWithExpectedSize(preparation.slotLinks.size());
		Set<String> primaryTags = preparation.slotProviders.knownPrimaryTags();
		for (SlotLink link : preparation.slotLinks.values()) {
			if (!primaryTags.contains(link.from)) {
				Kiwi.LOGGER.error("Unknown primary tag in \"from\": %s".formatted(link.from));
				continue;
			}
			if (!primaryTags.contains(link.to)) {
				Kiwi.LOGGER.error("Unknown primary tag in \"to\": %s".formatted(link.to));
				continue;
			}
			Pair<String, String> key = link.from.compareTo(link.to) <= 0 ? Pair.of(link.from, link.to) : Pair.of(link.to, link.from);
			SlotLink oldLink = map.put(key, link);
			if (oldLink != null) {
				Kiwi.LOGGER.error("Duplicate link: %s and %s".formatted(link, oldLink));
			}
		}
		LOOKUP = ImmutableMap.copyOf(map);
	}

	public static SlotLink create(
			String from,
			String to,
			int interest,
			List<TagTest> testTag,
			Pair<ResultAction, ResultAction> onLink,
			Pair<ResultAction, ResultAction> onUnlink) {
		return new SlotLink(
				from,
				to,
				interest,
				testTag,
				onLink.getFirst(),
				onLink.getSecond(),
				onUnlink.getFirst(),
				onUnlink.getSecond());
	}

	public record TagTest(String key, TagTestOperator operator) {
		public static final Codec<TagTest> CODEC = CustomizationCodecs.withAlternative(RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("key").forGetter(TagTest::key),
				TagTestOperator.CODEC.fieldOf("operator").forGetter(TagTest::operator)
		).apply(instance, TagTest::new)), ExtraCodecs.NON_EMPTY_STRING.xmap(s -> new TagTest(s, TagTestOperator.EQUAL), TagTest::key));
	}

	public record ResultAction(Map<String, String> setProperties) {
		public static final Codec<ResultAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("set_properties").forGetter(ResultAction::setProperties)
		).apply(instance, ResultAction::new));

		private static final ResultAction EMPTY = new ResultAction(Map.of());

		public BlockState apply(BlockState blockState) {
			for (Map.Entry<String, String> entry : setProperties.entrySet()) {
				blockState = KBlockUtils.setValueByString(blockState, entry.getKey(), entry.getValue());
			}
			return blockState;
		}
	}

	private static ImmutableMap<Pair<String, String>, SlotLink> LOOKUP = ImmutableMap.of();

	@Nullable
	public static SlotLink find(PlaceSlot slot1, PlaceSlot slot2) {
		String key1 = slot1.primaryTag();
		String key2 = slot2.primaryTag();
		Pair<String, String> key = isUprightLink(slot1, slot2) ? Pair.of(key1, key2) : Pair.of(key2, key1);
		return LOOKUP.get(key);
	}

	@Nullable
	public static MatchResult find(Collection<PlaceSlot> slots1, Collection<PlaceSlot> slots2) {
		if (slots1.isEmpty() || slots2.isEmpty()) {
			return null;
		}
		int maxInterest = 0;
		SlotLink matchedLink = null;
		boolean isUpright = false;
		for (PlaceSlot slot1 : slots1) {
			for (PlaceSlot slot2 : slots2) {
				SlotLink link = SlotLink.find(slot1, slot2);
				if (link != null && link.interest() > maxInterest && link.matches(slot1, slot2)) {
					maxInterest = link.interest();
					matchedLink = link;
					isUpright = isUprightLink(slot1, slot2) == link.from().compareTo(link.to()) <= 0;
				}
			}
		}
		return matchedLink == null ? null : new MatchResult(matchedLink, isUpright);
	}

	@Nullable
	public static MatchResult find(BlockState ourState, BlockState theirState, Direction direction) {
		Collection<PlaceSlot> slots1 = PlaceSlot.find(ourState, direction);
		Collection<PlaceSlot> slots2 = PlaceSlot.find(theirState, direction.getOpposite());
		return find(slots1, slots2);
	}

	public record MatchResult(SlotLink link, boolean isUpright) {
		public ResultAction onLinkFrom() {
			return isUpright ? link.onLinkFrom : link.onLinkTo;
		}

		public ResultAction onLinkTo() {
			return isUpright ? link.onLinkTo : link.onLinkFrom;
		}

		public ResultAction onUnlinkTo() {
			return isUpright ? link.onUnlinkTo : link.onUnlinkFrom;
		}
	}

	public static boolean isUprightLink(PlaceSlot slot1, PlaceSlot slot2) {
		return slot1.primaryTag().compareTo(slot2.primaryTag()) <= 0;
	}

	public boolean matches(PlaceSlot slot1, PlaceSlot slot2) {
		for (TagTest test : testTag) {
			String s1 = slot1.tags().get(test.key);
			String s2 = slot2.tags().get(test.key);
			if (s1 == null || s2 == null) {
				return false;
			}
			if (!test.operator.test().test(s1, s2)) {
				return false;
			}
		}
		return true;
	}
}
