package snownee.kiwi;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import snownee.kiwi.crafting.NoContainersShapedRecipe;
import snownee.kiwi.crafting.NoContainersShapelessRecipe;
import snownee.kiwi.crafting.TextureBlockRecipe;

public class KiwiManager {
    public static final Map<ResourceLocation, ModuleInfo> MODULES = Maps.newLinkedHashMap();

    public static IRecipeSerializer<?> shapedSerializer;
    public static IRecipeSerializer<?> shapelessSerializer;
    public static IRecipeSerializer<?> textureBlockSerializer;

    static {
        CrashReportExtender.registerCrashCallable("Kiwi Modules", () -> {
            return "\n" + MODULES.keySet().stream().map(ResourceLocation::toString).sorted(StringUtils::compare).collect(Collectors.joining("\n\t\t", "\t\t", ""));
        });
    }

    private KiwiManager() {}

    public static void addInstance(ResourceLocation resourceLocation, AbstractModule module, ModContext context) {
        if (MODULES.containsKey(resourceLocation)) {
            Kiwi.logger.error(Kiwi.MARKER, "Found a duplicate module name {}, skipping.", resourceLocation);
        } else {
            MODULES.put(resourceLocation, new ModuleInfo(resourceLocation, module, context));
        }
    }

    @Deprecated
    public static void addItemGroup(String modId, String name, ItemGroup group) {}

    static void handleRegister(RegistryEvent.Register<?> event) {
        if (event.getRegistry() == ForgeRegistries.RECIPE_SERIALIZERS) {
            IForgeRegistry<IRecipeSerializer<?>> registry = (IForgeRegistry<IRecipeSerializer<?>>) event.getRegistry();
            registry.register(shapedSerializer = new NoContainersShapedRecipe.Serializer().setRegistryName(Kiwi.MODID, "shaped_no_containers"));
            registry.register(shapelessSerializer = new NoContainersShapelessRecipe.Serializer().setRegistryName(Kiwi.MODID, "shapeless_no_containers"));
            registry.register(textureBlockSerializer = new TextureBlockRecipe.Serializer().setRegistryName(Kiwi.MODID, "texture_block"));
        }
        MODULES.values().forEach(info -> info.handleRegister(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

}
