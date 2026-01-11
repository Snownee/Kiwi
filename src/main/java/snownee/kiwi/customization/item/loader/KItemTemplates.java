package snownee.kiwi.customization.item.loader;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;

@KiwiModule("item_templates")
public class KItemTemplates extends AbstractModule {
	@KiwiModule.Name("minecraft:simple")
	public static final KiwiGO<KItemTemplate.Type<SimpleItemTemplate>> SIMPLE = register(SimpleItemTemplate::directCodec);
	@KiwiModule.Name("minecraft:built_in")
	public static final KiwiGO<KItemTemplate.Type<BuiltInItemTemplate>> BUILT_IN = register(BuiltInItemTemplate::directCodec);
	@KiwiModule.Name("minecraft:block")
	public static final KiwiGO<KItemTemplate.Type<BlockItemTemplate>> BLOCK = register(BlockItemTemplate::directCodec);

	private static <T extends KItemTemplate> KiwiGO<KItemTemplate.Type<T>> register(Supplier<Codec<T>> codec) {
		return go(() -> new KItemTemplate.Type<>(codec));
	}
}
