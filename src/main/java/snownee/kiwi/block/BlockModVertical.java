package snownee.kiwi.block;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

@SuppressWarnings("deprecation")
public class BlockModVertical extends BlockMod
{

    public BlockModVertical(String name, Material materialIn)
    {
        super(name, materialIn);
        setDefaultState(blockState.getBaseState().withProperty(BlockSlab.HALF, EnumBlockHalf.BOTTOM));
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(BlockSlab.HALF, EnumBlockHalf.values()[meta & 1]);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP ? 0 : 1;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, BlockSlab.HALF);
    }

}
