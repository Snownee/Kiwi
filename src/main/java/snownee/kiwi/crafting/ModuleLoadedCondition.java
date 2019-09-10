package snownee.kiwi.crafting;

import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import snownee.kiwi.Kiwi;

public class ModuleLoadedCondition implements ICondition
{
    public static final ResourceLocation ID = new ResourceLocation(Kiwi.MODID, "is_loaded");

    final ResourceLocation module;

    public ModuleLoadedCondition(ResourceLocation module) {
        this.module = module;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test() {
        return Kiwi.isLoaded(this.module);
    }

    public static final class Serializer implements IConditionSerializer<ModuleLoadedCondition> {

        @Override
        public void write(JsonObject json, ModuleLoadedCondition condition) {
            json.addProperty("module", condition.module.toString());
        }

        @Override
        public ModuleLoadedCondition read(JsonObject json) {
            ResourceLocation module = new ResourceLocation(JSONUtils.getString(json, "module"));
            return new ModuleLoadedCondition(module);
        }

        @Override
        public ResourceLocation getID() {
            return ModuleLoadedCondition.ID;
        }
    }

}
