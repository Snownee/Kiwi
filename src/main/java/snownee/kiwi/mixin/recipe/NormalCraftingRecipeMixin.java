package snownee.kiwi.mixin.recipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import snownee.kiwi.recipe.KiwiRecipe;

@Mixin(NormalCraftingRecipe.class)
public class NormalCraftingRecipeMixin implements KiwiRecipe {
	@Unique
	private boolean kiwi$noRemainders;

	@Override
	public void kiwi$setNoRemainders(boolean bl) {
		kiwi$noRemainders = bl;
	}

	@Override
	public boolean kiwi$noRemainders() {
		return kiwi$noRemainders;
	}
}
