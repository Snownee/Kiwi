package snownee.kiwi.mixin.codec;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.util.codec.IngredientCodecs;

@Mixin(Ingredient.ItemValue.class)
public class Ingredient_ItemValueMixin {
	@ModifyArg(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					remap = false,
					target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
	private static Function<RecordCodecBuilder.Instance<Ingredient.ItemValue>, ? extends App<RecordCodecBuilder.Mu<Ingredient.ItemValue>, Ingredient.ItemValue>> lychee$assignCodec(
			final Function<RecordCodecBuilder.Instance<Ingredient.ItemValue>, ? extends App<RecordCodecBuilder.Mu<Ingredient.ItemValue>, Ingredient.ItemValue>> builder) {
		IngredientCodecs.ITEM_VALUE_MAP_CODEC = RecordCodecBuilder.mapCodec(builder);
		return builder;
	}
}