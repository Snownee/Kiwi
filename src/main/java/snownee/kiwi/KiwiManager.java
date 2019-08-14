package snownee.kiwi;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import snownee.kiwi.crafting.NoContainersShapedRecipe;
import snownee.kiwi.crafting.NoContainersShapelessRecipe;
import snownee.kiwi.crafting.TextureBlockRecipe;

@EventBusSubscriber(modid = Kiwi.MODID, bus = Bus.MOD)
public class KiwiManager
{
    public static final Map<ResourceLocation, ModuleInfo> MODULES = Maps.newHashMap();
    public static final Set<ResourceLocation> ENABLED_MODULES = Sets.newHashSet();
    static Map<String, ItemGroup> GROUPS = Maps.newHashMap();

    public static IRecipeSerializer<?> shapedSerializer;
    public static IRecipeSerializer<?> shapelessSerializer;
    public static IRecipeSerializer<?> textureBlockSerializer;

    static
    {
        CrashReportExtender.registerCrashCallable("Kiwi Modules", () -> {
            return "\n" + ENABLED_MODULES.stream().map(ResourceLocation::toString).sorted(StringUtils::compare).collect(Collectors.joining("\n\t\t", "\t\t", ""));
        });
    }

    private KiwiManager()
    {
    }

    public static void addInstance(ResourceLocation resourceLocation, AbstractModule module, ModContext context)
    {
        if (MODULES.containsKey(resourceLocation))
        {
            Kiwi.logger.error(Kiwi.MARKER, "Found a duplicate module name {}, skipping.", resourceLocation);
        }
        else
        {
            MODULES.put(resourceLocation, new ModuleInfo(resourceLocation, module, context));
            ENABLED_MODULES.add(resourceLocation);
        }
    }

    public static void addItemGroup(String modId, String name, ItemGroup group)
    {
        GROUPS.put(modId + ":" + name, group);
    }

    public static void handleRegister(RegistryEvent.Register<?> event)
    {
        MODULES.values().forEach(info -> info.handleRegister(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    @SubscribeEvent
    public static void registerRecipeTypes(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry().register(shapedSerializer = new NoContainersShapedRecipe.Serializer().setRegistryName(Kiwi.MODID, "shaped_no_containers"));
        event.getRegistry().register(shapelessSerializer = new NoContainersShapelessRecipe.Serializer().setRegistryName(Kiwi.MODID, "shapeless_no_containers"));
        event.getRegistry().register(textureBlockSerializer = new TextureBlockRecipe.Serializer().setRegistryName(Kiwi.MODID, "texture_block"));
    }

}
