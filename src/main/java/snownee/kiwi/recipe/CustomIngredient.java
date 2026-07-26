package snownee.kiwi.recipe;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public interface CustomIngredient {
	/**
	 * Checks if a stack matches this ingredient.
	 * The stack <strong>must not</strong> be modified in any way.
	 *
	 * @param stack the stack to test
	 * @return {@code true} if the stack matches this ingredient, {@code false} otherwise
	 */
	boolean test(ItemStack stack);

	/**
	 * {@return the list of stacks that match this ingredient.}
	 *
	 * <p>The following guidelines should be followed for good compatibility:
	 * <ul>
	 *     <li>These stacks are generally used for display purposes, and need not be exhaustive or perfectly accurate.</li>
	 *     <li>An exception is ingredients that {@linkplain #requiresTesting() don't require testing},
	 *     for which it is important that the returned stacks correspond exactly to all the accepted {@link Item}s.</li>
	 *     <li>The ingredient should try to return at least one stack with each accepted {@link Item}.
	 *     This allows mods that inspect the ingredient to figure out which stacks it might accept.</li>
	 * </ul>
	 *
	 * <p>Note: no caching needs to be done by the implementation, this is already handled by the ingredient itself.
	 */
	Stream<Holder<Item>> items();

	/**
	 * Returns whether this ingredient always requires {@linkplain #test direct stack testing}.
	 *
	 * @return {@code false} if this ingredient ignores NBT data when matching stacks, {@code true} otherwise
	 */
	boolean requiresTesting();

	/**
	 * {@return the serializer for this ingredient}
	 *
	 * <p>The serializer must have been registered using {@link net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer#register}.
	 */
	CustomIngredientSerializer<?> getSerializer();

	/**
	 * Returns a {@link SlotDisplay} representing this ingredient, this is synced to the client to display in the recipe book.
	 *
	 * @return a {@link SlotDisplay} instance.
	 */
	default SlotDisplay display() {
		// Matches the vanilla logic in Ingredient.display()
		return new SlotDisplay.Composite(items().map(Ingredient::displayForSingleItem).toList());
	}

	/**
	 * {@return a new {@link Ingredient } behaving as defined by this custom ingredient}.
	 */
	@ApiStatus.NonExtendable
	default Ingredient toVanilla() {
		return new CustomIngredientImpl<>(this).toVanilla();
	}
}
