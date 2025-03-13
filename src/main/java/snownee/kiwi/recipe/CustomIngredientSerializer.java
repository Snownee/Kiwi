package snownee.kiwi.recipe;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Serializer for a {@link CustomIngredient}.
 *
 * <p>All instances must be registered using {@link #register} for deserialization to work.
 *
 * @param <T> the type of the custom ingredient
 */
public interface CustomIngredientSerializer<T extends CustomIngredient> {
	/**
	 * Registers a custom ingredient serializer, using the {@linkplain CustomIngredientSerializer#getIdentifier() serializer's identifier}.
	 *
	 * @throws IllegalArgumentException if the serializer is already registered
	 */
	static void register(CustomIngredientSerializer<?> serializer) {
		CustomIngredientImpl.registerSerializer(serializer);
	}

	/**
	 * {@return the custom ingredient serializer registered with the given identifier, or {@code null} if there is no such serializer}.
	 */
	@Nullable
	static CustomIngredientSerializer<?> get(ResourceLocation identifier) {
		return CustomIngredientImpl.getSerializer(identifier);
	}

	/**
	 * {@return the identifier of this serializer}.
	 */
	ResourceLocation getIdentifier();

	/**
	 * {@return the codec}.
	 *
	 * <p>Codecs are used to read the ingredient from the recipe JSON files.
	 *
	 * @see Ingredient#CODEC
	 * @see Ingredient#CODEC_NONEMPTY
	 */
	MapCodec<T> getCodec(boolean allowEmpty);

	/**
	 * {@return the packet codec for serializing this ingredient}.
	 *
	 * @see Ingredient#CONTENTS_STREAM_CODEC
	 */
	StreamCodec<RegistryFriendlyByteBuf, T> getPacketCodec();
}