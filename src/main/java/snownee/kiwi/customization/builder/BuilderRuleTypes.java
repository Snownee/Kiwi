package snownee.kiwi.customization.builder;

import com.mojang.serialization.MapCodec;

import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;

@KiwiModule("builder_rules")
public class BuilderRuleTypes extends AbstractModule {
	@KiwiModule.Name("minecraft:replace_in_hand")
	public static final KiwiGO<BuilderRule.Type<ReplaceInHandRule>> REPLACE_IN_HAND = register(ReplaceInHandRule.CODEC);
	@KiwiModule.Name("minecraft:cycle_property")
	public static final KiwiGO<BuilderRule.Type<CyclePropertyRule>> CYCLE_PROPERTY = register(CyclePropertyRule.CODEC);

	private static <T extends BuilderRule> KiwiGO<BuilderRule.Type<T>> register(MapCodec<T> codec) {
		return go(() -> new BuilderRule.Type<>(codec.codec()));
	}
}
