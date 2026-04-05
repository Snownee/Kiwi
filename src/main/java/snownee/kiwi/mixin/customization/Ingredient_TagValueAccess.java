package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.crafting.Ingredient;

@Mixin(Ingredient.class)
public interface Ingredient_TagValueAccess {
}
