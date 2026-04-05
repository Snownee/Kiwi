package snownee.kiwi.data;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiCommonConfig;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.LoadingContext;
import snownee.kiwi.recipe.AlternativesIngredient;
import snownee.kiwi.recipe.CustomIngredientSerializer;
import snownee.kiwi.recipe.EvalCondition;
import snownee.kiwi.recipe.ModuleLoadedCondition;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.kiwi.recipe.crafting.KiwiShapelessRecipe;
import snownee.kiwi.recipe.crafting.NoContainersShapedRecipe;

@KiwiModule("data")
public final class DataModule extends AbstractModule {

	public static final KiwiGO<RecipeSerializer<NoContainersShapedRecipe>> SHAPED_NO_CONTAINERS = go(NoContainersShapedRecipe.Serializer::new);
	public static final KiwiGO<RecipeSerializer<KiwiShapelessRecipe>> SHAPELESS = go(KiwiShapelessRecipe.Serializer::new);
	public static final KiwiGO<MapCodec<ModuleLoadedCondition>> IS_LOADED = go(
			() -> ModuleLoadedCondition.CODEC,
			NeoForgeRegistries.Keys.CONDITION_CODECS);
	public static final KiwiGO<MapCodec<EvalCondition>> EVAL = go(
			() -> EvalCondition.CODEC,
			NeoForgeRegistries.Keys.CONDITION_CODECS);
	public static final KiwiGO<SlotDisplay.Type<?>> SIZED = go(() -> new SlotDisplay.Type<>(
			SizedIngredient.SizedSlotDisplay.MAP_CODEC,
			SizedIngredient.SizedSlotDisplay.STREAM_CODEC));

	public DataModule() {
		CustomIngredientSerializer.register(AlternativesIngredient.SERIALIZER);
	}

	@KiwiModule.LoadingCondition("data")
	public static boolean shouldLoad(LoadingContext ctx) {
		return Kiwi.enableDataModule || KiwiCommonConfig.getBooleanVar("EnableDataModule");
	}
}
