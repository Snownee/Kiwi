//package snownee.kiwi.block;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockFenceGate;
//import net.minecraft.block.BlockPlanks;
//import net.minecraft.block.material.MapColor;
//import net.minecraft.block.material.Material;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.client.renderer.block.statemap.StateMap;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockAccess;
//import net.minecraftforge.client.model.ModelLoader;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//
//public class BlockModFenceGate extends BlockFenceGate implements IModBlock
//{
//    private final String name;
//    private final IBlockState baseState;
//
//    @SuppressWarnings("deprecation")
//    public BlockModFenceGate(String name, IBlockState baseState)
//    {
//        super(BlockPlanks.EnumType.OAK);
//        this.name = name;
//        this.baseState = baseState;
//        setSoundType(baseState.getBlock().getSoundType());
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void mapModel()
//    {
//        ModelLoader.setCustomStateMapper(this, new StateMap.Builder().ignore(BlockFenceGate.POWERED).build());
//        IModBlock.super.mapModel();
//    }
//
//    @Override
//    public Block cast()
//    {
//        return this;
//    }
//
//    @Override
//    public String getName()
//    {
//        return name;
//    }
//
//    @Override
//    public void register(String modid)
//    {
//        setRegistryName(modid, getName());
//        setTranslationKey(modid + "." + getName());
//    }
//
//    @Override
//    public Material getMaterial(IBlockState state)
//    {
//        return baseState.getMaterial();
//    }
//
//    @Override
//    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos)
//    {
//        return baseState.getMapColor(worldIn, pos);
//    }
//
//}
