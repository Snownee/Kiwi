package snownee.kiwi.customization.block.loader;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;

@KiwiModule("block_templates")
public class KBlockTemplates extends AbstractModule {
	@KiwiModule.Name("minecraft:simple")
	public static final KiwiGO<KBlockTemplate.Type<SimpleBlockTemplate>> SIMPLE = register(SimpleBlockTemplate::directCodec);
	@KiwiModule.Name("minecraft:built_in")
	public static final KiwiGO<KBlockTemplate.Type<BuiltInBlockTemplate>> BUILT_IN = register(BuiltInBlockTemplate::directCodec);

	private static <T extends KBlockTemplate> KiwiGO<KBlockTemplate.Type<T>> register(Function<MapCodec<Optional<KMaterial>>, Codec<T>> codec) {
		return go(() -> new KBlockTemplate.Type<>(codec));
	}
}
