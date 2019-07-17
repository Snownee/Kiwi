package snownee.kiwi.item;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ModBlockItem extends BlockItem
{
    public static final Set<TileEntityType<?>> INSTANT_UPDATE_TILES = FMLEnvironment.dist == Dist.CLIENT ? Sets.newHashSet() : null;

    public ModBlockItem(Block block, Item.Properties builder)
    {
        super(block, builder);
    }

    @Override
    protected boolean onBlockPlaced(BlockPos pos, World worldIn, PlayerEntity player, ItemStack stack, BlockState state)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (worldIn.isRemote && tile != null && INSTANT_UPDATE_TILES.contains(tile.getType()))
        {
            CompoundNBT data = stack.getChildTag("BlockEntityTag");
            if (data != null)
            {
                data = data.copy();
                data.putInt("x", pos.getX());
                data.putInt("y", pos.getY());
                data.putInt("z", pos.getZ());
                tile.read(data);
            }
        }
        return super.onBlockPlaced(pos, worldIn, player, stack, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ModItem.addTip(stack, worldIn, tooltip, flagIn);
    }
}
