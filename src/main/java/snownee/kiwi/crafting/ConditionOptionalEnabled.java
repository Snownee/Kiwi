package snownee.kiwi.crafting;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;

import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import snownee.kiwi.Kiwi;

public class ConditionOptionalEnabled implements IConditionFactory
{

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json)
    {
        String module = JsonUtils.getString(json, "module");
        return () -> Kiwi.isOptionalModuleLoaded(module);
    }

}
