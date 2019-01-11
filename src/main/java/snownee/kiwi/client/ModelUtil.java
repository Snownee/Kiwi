package snownee.kiwi.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import snownee.kiwi.Kiwi;
import snownee.kiwi.item.IVariant;
import snownee.kiwi.util.VariantsHolder.Variant;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public final class ModelUtil
{
    private ModelUtil()
    {
        throw new UnsupportedOperationException();
    }

    public static void mapItemModel(Item item)
    {
        ResourceLocation registryName;
        if (item == null || (registryName = item.getRegistryName()) == null)
        {
            Kiwi.logger.warn("Detecting unnamed item {}, skip its model setup", item);
            return;
        }
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(registryName, "inventory"));
    }

    public static void mapItemVariantsModel(Item item, String prefix, List<? extends Variant<? extends IStringSerializable>> variants, String suffix)
    {
        String modID = Objects.requireNonNull(item.getRegistryName()).getNamespace();
        variants.forEach(variant -> ModelLoader.setCustomModelResourceLocation(item, variant.getMeta(), new ModelResourceLocation(modID + ":" + prefix + variant.getValue().getName() + suffix, "inventory")));
    }

    public static void mapItemVariantsModelNew(Item item, String prefix, IVariant<?>[] variants, String suffix)
    {
        String modID = Objects.requireNonNull(item.getRegistryName()).getNamespace();
        Arrays.asList(variants).forEach(variant -> ModelLoader.setCustomModelResourceLocation(item, variant.getMeta(), new ModelResourceLocation(modID + ":" + prefix + variant.getName() + suffix, "inventory")));
    }

    public static void mapFluidModel(BlockFluidBase fluidBlock)
    {
        Fluid fluid = fluidBlock.getFluid();
        final String modID = Objects.requireNonNull(fluidBlock.getRegistryName()).getNamespace();
        FluidCustomModelMapper mapper = new FluidCustomModelMapper(modID, fluid);
        ModelLoader.setCustomStateMapper(fluidBlock, mapper);
    }

    public static class FluidCustomModelMapper extends StateMapperBase
    {

        private final ModelResourceLocation res;

        FluidCustomModelMapper(String modid, Fluid f)
        {
            this.res = new ModelResourceLocation(new ResourceLocation(modid, "fluid"), f.getName());
        }

        @Override
        public ModelResourceLocation getModelResourceLocation(IBlockState state)
        {
            return res;
        }

    }
}
