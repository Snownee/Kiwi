package snownee.kiwi.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.WoodButtonBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.DeferredActions;

/**
 *
 * @author Snownee
 *
 */
public class ModBlock extends Block {

	public ModBlock(Block.Properties builder) {
		super(builder);
	}

	public static SoundType deduceSoundType(final Material material) {
		if (material == Material.WOOD || material == Material.VEGETABLE) {
			return SoundType.WOOD;
		}
		if (material == Material.DIRT || material == Material.CLAY) {
			return SoundType.GRAVEL;
		}
		if (material == Material.PLANT || material == Material.GRASS || material == Material.REPLACEABLE_PLANT || material == Material.LEAVES || material == Material.SPONGE || material == Material.EXPLOSIVE) {
			return SoundType.GRASS;
		}
		if (material == Material.REPLACEABLE_WATER_PLANT || material == Material.WATER_PLANT) {
			return SoundType.WET_GRASS;
		}
		if (material == Material.METAL) {
			return SoundType.METAL;
		}
		if (material == Material.GLASS || material == Material.PORTAL || material == Material.ICE || material == Material.ICE_SOLID || material == Material.BUILDABLE_GLASS) {
			return SoundType.GLASS;
		}
		if (material == Material.WOOL || material == Material.CLOTH_DECORATION || material == Material.CACTUS || material == Material.CAKE || material == Material.FIRE) {
			return SoundType.WOOL;
		}
		if (material == Material.SAND) {
			return SoundType.SAND;
		}
		if (material == Material.SNOW || material == Material.TOP_SNOW) {
			return SoundType.SNOW;
		}
		if (material == Material.HEAVY_METAL) {
			return SoundType.ANVIL;
		}
		return SoundType.STONE;
	}

	public static float deduceHardness(final Material material) {
		if (material == Material.PLANT || material == Material.AIR || material == Material.FIRE) {
			return 0;
		}
		if (material == Material.STONE) {
			return 2.5F;
		}
		if (material == Material.WOOD) {
			return 2;
		}
		if (material == Material.GRASS) {
			return 0.6F;
		}
		if (material == Material.SAND || material == Material.DIRT || material == Material.CLAY) {
			return 0.5F;
		}
		if (material == Material.GLASS) {
			return 0.3F;
		}
		if (material == Material.CACTUS) {
			return 0.4F;
		}
		if (material == Material.METAL || material == Material.HEAVY_METAL) {
			return 5;
		}
		if (material == Material.WEB) {
			return 4;
		}
		if (material == Material.WOOL) {
			return 0.8F;
		}
		if (material == Material.WATER || material == Material.LAVA) {
			return 100;
		}
		return 1;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return pickBlock(state, target, world, pos, player);
	}

	@SuppressWarnings("deprecation")
	public static ItemStack pickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack stack = state.getBlock().getCloneItemStack(world, pos, state);
		TileEntity tile = world.getBlockEntity(pos);
		if (tile instanceof BaseTile && !tile.onlyOpCanSetNbt() && ((BaseTile) tile).persistData) {
			CompoundNBT data = tile.save(new CompoundNBT());
			data.remove("x");
			data.remove("y");
			data.remove("z");
			data.remove("id");
			stack.addTagElement("BlockEntityTag", data);
		}
		return stack;
	}

	/**
	 * @since 3.5.0
	 */
	public static void setFireInfo(Block block) {
		Material material = block.material;
		int fireSpreadSpeed = 0;
		int flammability = 0;
		if (material == Material.WOOD) {
			if ((!(block instanceof DoorBlock) && !(block instanceof TrapDoorBlock) && !(block instanceof WoodButtonBlock) && !(block instanceof PressurePlateBlock))) {
				fireSpreadSpeed = 5;
				flammability = 20;
			}
		} else if (material == Material.PLANT || material == Material.REPLACEABLE_PLANT) {
			if (!(block instanceof SaplingBlock)) {
				fireSpreadSpeed = 30;
				flammability = 100;
			}
		} else if (material == Material.CLOTH_DECORATION) {
			fireSpreadSpeed = 60;
			flammability = 20;
		} else if (material == Material.LEAVES) {
			fireSpreadSpeed = 30;
			flammability = 60;
		} else if (material == Material.WOOL) {
			fireSpreadSpeed = 30;
			flammability = 60;
		}
		if (fireSpreadSpeed != 0) {
			DeferredActions.setFireInfo(block, fireSpreadSpeed, flammability);
		}
	}

}
