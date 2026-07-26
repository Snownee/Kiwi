package snownee.kiwi.data;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import snownee.kiwi.Kiwi;

public record NeoIngredientWrapper(Type type, ICustomIngredient ingredient) implements CustomIngredient {
	public static void wrapAll() {
		for (Holder<IngredientType<?>> holder : NeoForgeRegistries.INGREDIENT_TYPES.asHolderIdMap()) {
			Identifier id = holder.unwrapKey().orElseThrow().identifier();
			if ("neoforge".equals(id.getNamespace()) || "fabric_recipe_api_v1".equals(id.getNamespace()) ||
					net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer.get(id) != null) {
				continue;
			}
			Kiwi.LOGGER.info("Registering NeoIngredientWrapper: {}", id);
			//noinspection unchecked,rawtypes
			net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer.register(new NeoIngredientWrapper.Type((Holder) (Object) holder));
		}
	}

	@Override
	public boolean test(ItemStack stack) {
		return ingredient.test(stack);
	}

	@Override
	public Stream<Holder<Item>> items() {
		return ingredient.items();
	}

	@Override
	public boolean requiresTesting() {
		return !ingredient.isSimple();
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return type;
	}

	@Override
	public SlotDisplay display() {
		return ingredient.display();
	}

	public static class Type implements CustomIngredientSerializer<NeoIngredientWrapper> {
		private final Identifier id;
		private final MapCodec<NeoIngredientWrapper> codec;
		private final StreamCodec<RegistryFriendlyByteBuf, NeoIngredientWrapper> streamCodec;

		public Type(Holder<IngredientType<ICustomIngredient>> holder) {
			this.id = holder.unwrapKey().orElseThrow().identifier();
			this.codec = holder.value().codec().xmap(ingr -> new NeoIngredientWrapper(this, ingr), wrapper -> wrapper.ingredient);
			this.streamCodec = holder.value().streamCodec().map(
					ingr -> new NeoIngredientWrapper(this, ingr),
					wrapper -> wrapper.ingredient).cast();
		}

		@Override
		public Identifier getIdentifier() {
			return id;
		}

		@Override
		public MapCodec<NeoIngredientWrapper> getCodec() {
			return codec;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, NeoIngredientWrapper> getStreamCodec() {
			return streamCodec;
		}
	}
}
