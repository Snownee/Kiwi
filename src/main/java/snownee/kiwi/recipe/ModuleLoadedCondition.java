package snownee.kiwi.recipe;

import java.util.function.Predicate;

import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.Util;

public enum ModuleLoadedCondition implements Predicate<JsonObject> {
	INSTANCE;

	public static final ResourceLocation ID = new ResourceLocation(Kiwi.ID, "is_loaded");

	public static Provider provider(ResourceLocation module) {
		return new Provider(module);
	}

	@Override
	public boolean test(JsonObject jsonObject) {
		return Kiwi.isLoaded(Util.RL(GsonHelper.getAsString(jsonObject, "module")));
	}

	public static class Provider implements ConditionJsonProvider {
		private final ResourceLocation module;

		protected Provider(ResourceLocation module) {
			this.module = module;
		}

		@Override
		public void writeParameters(JsonObject json) {
			json.addProperty("module", module.toString());
		}

		@Override
		public ResourceLocation getConditionId() {
			return ModuleLoadedCondition.ID;
		}
	}

}
