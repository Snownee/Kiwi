package snownee.kiwi.customization.block.loader;

import java.util.Optional;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import snownee.kiwi.customization.CustomizationRegistries;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class KBlockTemplate {

	public static Codec<KBlockTemplate> codec(MapCodec<Optional<KMaterial>> materialCodec) {
		return CustomizationRegistries.BLOCK_TEMPLATE.byNameCodec().dispatch(
				"type",
				KBlockTemplate::type,
				type -> type.codec().apply(materialCodec));
	}

	protected final Optional<BlockDefinitionProperties> properties;

	protected KBlockTemplate(Optional<BlockDefinitionProperties> properties) {
		this.properties = properties;
	}

	public abstract Type<?> type();

	public abstract void resolve(ResourceLocation key);

	abstract Block createBlock(ResourceLocation id, BlockBehaviour.Properties properties, JsonObject input);

	public final Optional<BlockDefinitionProperties> properties() {
		return properties;
	}

	public record Type<T extends KBlockTemplate>(Function<MapCodec<Optional<KMaterial>>, Codec<T>> codec) {}
}
