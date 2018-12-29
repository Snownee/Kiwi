package snownee.kiwi.crafting;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import snownee.kiwi.Kiwi;

public class ConditionModuleLoaded implements IConditionFactory
{

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json)
    {
        ResourceLocation module = new ResourceLocation(JsonUtils.getString(json, "module"));
        return () -> Kiwi.isLoaded(module);
    }

}
