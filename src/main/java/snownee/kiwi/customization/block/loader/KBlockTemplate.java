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
import snownee.kiwi.customization.block.BlockFundamentals;
import snownee.kiwi.util.resource.OneTimeLoader;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class KBlockTemplate {

	public static Codec<KBlockTemplate> codec(BlockFundamentals.CodecCreationContext context) {
		return CustomizationRegistries.BLOCK_TEMPLATE.byNameCodec().dispatch(
				"type",
				KBlockTemplate::type,
				type -> type.codec().apply(context));
	}

	protected final Optional<BlockDefinitionProperties> properties;

	protected KBlockTemplate(Optional<BlockDefinitionProperties> properties) {
		this.properties = properties;
	}

	public abstract Type<?> type();

	public abstract void resolve(ResourceLocation key, OneTimeLoader.Context context);

	abstract Block createBlock(ResourceLocation id, BlockBehaviour.Properties properties, JsonObject input);

	public final Optional<BlockDefinitionProperties> properties() {
		return properties;
	}

	public record Type<T extends KBlockTemplate>(Function<BlockFundamentals.CodecCreationContext, MapCodec<T>> codec) {}
}
