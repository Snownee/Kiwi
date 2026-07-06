package snownee.kiwi.data;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiCommonConfig;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.LoadingContext;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.recipe.AlternativesIngredient;
import snownee.kiwi.recipe.AlternativesIngredientBuilder;
import snownee.kiwi.recipe.EvalCondition;
import snownee.kiwi.recipe.ModuleLoadedCondition;
import snownee.kiwi.recipe.SizedIngredient;

@KiwiModule("data")
public final class DataModule extends AbstractModule {

	public static final KiwiGO<SlotDisplay.Type<?>> SIZED = go(() -> new SlotDisplay.Type<>(
			SizedIngredient.SizedSlotDisplay.MAP_CODEC,
			SizedIngredient.SizedSlotDisplay.STREAM_CODEC));

	@Override
	protected void addEntries() {
		ResourceConditions.register(ModuleLoadedCondition.TYPE);
		ResourceConditions.register(EvalCondition.TYPE);
		if (Platform.isDataGen()) {
			CustomIngredientSerializer.register(AlternativesIngredientBuilder.Serializer.INSTANCE);
		} else {
			CustomIngredientSerializer.register(AlternativesIngredient.Serializer.INSTANCE);
		}
	}

	@KiwiModule.LoadingCondition("data")
	public static boolean shouldLoad(LoadingContext ctx) {
		return Kiwi.enableDataModule || KiwiCommonConfig.getBooleanVar("EnableDataModule");
	}
}
