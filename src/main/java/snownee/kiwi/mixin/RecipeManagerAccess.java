package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccess {

	@Accessor(remap = false)
	IContext getContext();

}
