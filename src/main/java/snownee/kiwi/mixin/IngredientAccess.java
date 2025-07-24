package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

@Mixin(Ingredient.class)
public interface IngredientAccess {
	@Accessor
	HolderSet<Item> getValues();
}
