package snownee.kiwi.test;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import snownee.kiwi.RenderLayer;
import snownee.kiwi.RenderLayer.Layer;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.crafting.FullBlockIngredient;
import snownee.kiwi.tile.TextureTile;

// fill ~-40 ~ ~-40 ~40 ~ ~40 kiwi:tex_block
@RenderLayer(Layer.CUTOUT)
public class TestBlock extends StairsBlock
{

    @SuppressWarnings("deprecation")
    public TestBlock(Properties builder)
    {
        super(Blocks.STONE.getDefaultState(), builder);
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new TestTile();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if (worldIn.isRemote)
        {
            return ActionResultType.SUCCESS;
        }
        TileEntity tile = worldIn.getTileEntity(pos);
        ItemStack stack = player.getHeldItem(handIn);
        if (tile instanceof TextureTile && !stack.isEmpty())
        {
            if (FullBlockIngredient.isFullBlock(stack))
            {
                TextureTile textureTile = (TextureTile) tile;
                BlockState state2 = ((BlockItem) stack.getItem()).getBlock().getDefaultState();
                textureTile.setTexture("top", state2);
                textureTile.setTexture("side", state2);
                textureTile.setTexture("bottom", state2);
                textureTile.refresh();
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.hasTileEntity() && state.getBlock() != newState.getBlock())
        {
            worldIn.removeTileEntity(pos);
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
    {
        return ModBlock.pickBlock(state, target, world, pos, player);
    }
}
