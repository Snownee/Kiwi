package snownee.kiwi.recipe;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record CustomIngredientImpl<T extends CustomIngredient>(T ingredient) implements ICustomIngredient {
	static final Map<Identifier, CustomIngredientSerializer<?>> REGISTERED_SERIALIZERS = new ConcurrentHashMap<>();
	private static final Map<CustomIngredientSerializer<?>, IngredientType<?>> INGREDIENT_TYPES = Maps.newIdentityHashMap();

	@SubscribeEvent
	private static void onRegister(RegisterEvent event) {
		event.register(
				NeoForgeRegistries.INGREDIENT_TYPES.key(), helper -> {
					INGREDIENT_TYPES.forEach((serializer, type) -> helper.register(serializer.getIdentifier(), type));
				});
	}

	public static void registerSerializer(CustomIngredientSerializer<?> serializer) {
		Objects.requireNonNull(serializer.getIdentifier(), "CustomIngredientSerializer identifier may not be null.");

		if (REGISTERED_SERIALIZERS.putIfAbsent(serializer.getIdentifier(), serializer) != null) {
			throw new IllegalArgumentException(
					"CustomIngredientSerializer with identifier " + serializer.getIdentifier() + " already registered.");
		}

		INGREDIENT_TYPES.put(serializer, makeSerializer(serializer));
	}

	private static <T extends CustomIngredient> IngredientType<CustomIngredientImpl<T>> makeSerializer(CustomIngredientSerializer<T> serializer) {
		MapCodec<CustomIngredientImpl<T>> codec = serializer.getCodec(true).xmap(
				CustomIngredientImpl::new,
				CustomIngredientImpl::ingredient);
		StreamCodec<RegistryFriendlyByteBuf, CustomIngredientImpl<T>> streamCodec = serializer.getPacketCodec().map(
				CustomIngredientImpl::new,
				CustomIngredientImpl::ingredient);
		return new IngredientType<>(codec, streamCodec);
	}

	@Nullable
	public static CustomIngredientSerializer<?> getSerializer(Identifier identifier) {
		Objects.requireNonNull(identifier, "Identifier may not be null.");

		return REGISTERED_SERIALIZERS.get(identifier);
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return ingredient.test(itemStack);
	}

	@Override
	public Stream<ItemStack> getItems() {
		return this.ingredient.getMatchingStacks().stream();
	}

	@Override
	public boolean isSimple() {
		return !ingredient.requiresTesting();
	}

	@Override
	public IngredientType<?> getType() {
		return Objects.requireNonNull(INGREDIENT_TYPES.get(ingredient.getSerializer()));
	}
}