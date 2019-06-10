//package snownee.kiwi.block;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockFence;
//import net.minecraft.block.material.MapColor;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockAccess;
//
//public class BlockModFence extends BlockFence implements IModBlock
//{
//    private final String name;
//    private final IBlockState baseState;
//
//    @SuppressWarnings("deprecation")
//    public BlockModFence(String name, IBlockState baseState)
//    {
//        super(baseState.getMaterial(), MapColor.AIR);
//        this.name = name;
//        this.baseState = baseState;
//        setSoundType(baseState.getBlock().getSoundType());
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
//    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos)
//    {
//        return baseState.getMapColor(worldIn, pos);
//    }
//
//}
