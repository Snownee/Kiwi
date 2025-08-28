package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.serialization.Codec;

import net.minecraft.world.item.crafting.Ingredient;

@Mixin(Ingredient.TagValue.class)
public interface Ingredient_TagValueAccess {
	@Accessor
	static Codec<Ingredient.TagValue> getCODEC() {
		throw new UnsupportedOperationException();
	}
}
