package snownee.kiwi.crafting;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;

import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionSerializer;
import snownee.kiwi.Kiwi;

public class ConditionModuleLoaded implements IConditionSerializer
{

    @Override
    public BooleanSupplier parse(JsonObject json)
    {
        ResourceLocation module = new ResourceLocation(JSONUtils.getString(json, "module"));
        return () -> Kiwi.isLoaded(module);
    }

}
