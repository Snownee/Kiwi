package snownee.kiwi.item;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.TagValueInput;
import snownee.kiwi.Kiwi;
import snownee.kiwi.block.IKiwiBlock;
import snownee.kiwi.loader.Platform;

public class ModBlockItem extends BlockItem implements ItemCategoryFiller {
	public static final Set<BlockEntityType<?>> INSTANT_UPDATE_TILES = Platform.isPhysicalClient() ? Sets.newHashSet() : null;

	public ModBlockItem(Block block, Properties builder) {
		super(block, builder.useBlockDescriptionPrefix());
	}

	@Override
	public InteractionResult place(BlockPlaceContext context) {
		InteractionResult result = super.place(context);
		Level worldIn = context.getLevel();
		if (!result.consumesAction() || !worldIn.isClientSide()) {
			return result;
		}
		BlockPos pos = context.getClickedPos();
		BlockEntity be = worldIn.getBlockEntity(pos);
		if (be != null && INSTANT_UPDATE_TILES.contains(be.getType())) {
			TypedEntityData<BlockEntityType<?>> data = context.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
			if (data != null) {
				try (
						ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(
								be.problemPath(),
								Kiwi.LOGGER)) {
					be.loadWithComponents(TagValueInput.create(scopedCollector, worldIn.registryAccess(), data.copyTagWithoutId()));
					be.setChanged();
				}
			}
		}
		return result;
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
