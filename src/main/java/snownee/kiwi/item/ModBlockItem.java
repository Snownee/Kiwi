package snownee.kiwi.item;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.block.IKiwiBlock;
import snownee.kiwi.loader.Platform;

public class ModBlockItem extends BlockItem implements ItemCategoryFiller {
	public static final Set<BlockEntityType<?>> INSTANT_UPDATE_TILES = Platform.isPhysicalClient() ? Sets.newHashSet() : null;

	public ModBlockItem(Block block, Properties builder) {
		super(block, builder);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level worldIn, @Nullable Player player, ItemStack stack, BlockState state) {
		if (worldIn.isClientSide()) {
			BlockEntity tile = worldIn.getBlockEntity(pos);
			if (tile != null && INSTANT_UPDATE_TILES.contains(tile.getType())) {
				TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
				if (data != null && data.type() == tile.getType()) {
					data.loadInto(tile, worldIn.registryAccess());
					tile.setChanged();
				}
			}
		}
		return super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
	}

	@Override
	public void appendHoverText(
			ItemStack itemStack,
			TooltipContext tooltipContext,
			TooltipDisplay tooltipDisplay,
			Consumer<Component> tooltipAdder,
			TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, tooltipAdder, tooltipFlag);
		if (Platform.isPhysicalClient() && !KiwiClientConfig.globalTooltip) {
			List<Component> tooltip = com.google.common.collect.Lists.newArrayList();
			ModItem.addTip(itemStack, tooltip, tooltipFlag);
			tooltip.forEach(tooltipAdder);
		}
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

	@Override
	public void fillItemCategory(CreativeModeTab tab, FeatureFlagSet flags, boolean hasPermissions, List<ItemStack> items) {
		if (getBlock() instanceof ItemCategoryFiller filler) {
			filler.fillItemCategory(tab, flags, hasPermissions, items);
		} else {
			items.add(new ItemStack(this));
		}
	}
}
