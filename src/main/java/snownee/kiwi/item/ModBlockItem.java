package snownee.kiwi.item;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.block.IKiwiBlock;

public class ModBlockItem extends BlockItem {
	public static final Set<BlockEntityType<?>> INSTANT_UPDATE_TILES = FMLEnvironment.dist == Dist.CLIENT ? Sets.newHashSet() : null;

	public ModBlockItem(Block block, Item.Properties builder) {
		super(block, builder);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level worldIn, Player player, ItemStack stack, BlockState state) {
		BlockEntity tile = worldIn.getBlockEntity(pos);
		if (worldIn.isClientSide && tile != null && INSTANT_UPDATE_TILES.contains(tile.getType())) {
			CompoundTag data = stack.getTagElement("BlockEntityTag");
			if (data != null) {
				data = data.copy();
				data.putInt("x", pos.getX());
				data.putInt("y", pos.getY());
				data.putInt("z", pos.getZ());
				tile.load(data);
			}
		}
		return super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (!KiwiClientConfig.globalTooltip)
			ModItem.addTip(stack, tooltip, flagIn);
	}

	@Override
	public Component getName(ItemStack pStack) {
		Block block = getBlock();
		if (block instanceof IKiwiBlock) {
			return ((IKiwiBlock) block).getName(pStack);
		} else {
			return super.getName(pStack);
		}
	}
}
