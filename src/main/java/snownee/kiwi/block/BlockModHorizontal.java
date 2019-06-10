//package snownee.kiwi.block;
//
//import net.minecraft.block.BlockHorizontal;
//import net.minecraft.block.material.Material;
//import net.minecraft.block.state.BlockStateContainer;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.util.EnumFacing;
//import net.minecraft.util.Mirror;
//import net.minecraft.util.Rotation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//
//@SuppressWarnings("deprecation")
//public class BlockModHorizontal extends BlockMod
//{
//
//    public BlockModHorizontal(String name, Material materialIn)
//    {
//        super(name, materialIn);
//        setDefaultState(blockState.getBaseState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH));
//    }
//
//    @Override
//    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
//    {
//        return getDefaultState().withProperty(BlockHorizontal.FACING, placer.getHorizontalFacing().getOpposite());
//    }
//
//    @Override
//    public IBlockState getStateFromMeta(int meta)
//    {
//        return getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.byHorizontalIndex(meta & 3));
//    }
//
//    @Override
//    public int getMetaFromState(IBlockState state)
//    {
//        return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
//    }
//
//    @Override
//    public IBlockState withRotation(IBlockState state, Rotation rot)
//    {
//        return state.withProperty(BlockHorizontal.FACING, rot.rotate(state.getValue(BlockHorizontal.FACING)));
//    }
//
//    @Override
//    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
//    {
//        return state.withRotation(mirrorIn.toRotation(state.getValue(BlockHorizontal.FACING)));
//    }
//
//    @Override
//    protected BlockStateContainer createBlockState()
//    {
//        return new BlockStateContainer(this, BlockHorizontal.FACING);
//    }
//
//}
