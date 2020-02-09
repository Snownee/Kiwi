package snownee.kiwi.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FireBlock;
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
import snownee.kiwi.AbstractModule;
import snownee.kiwi.tile.BaseTile;

/**
 * 
 * Simple base block. You do not have to set sound or hardness every time.
 * 
 * If you want to extends other vanilla class, simply use
 * {@link AbstractModule#init(T block)} to automatically set properties
 * 
 * @author Snownee
 * 
 */
public class ModBlock extends Block {
    public ModBlock(Block.Properties builder) {
        super(builder);
        deduceSoundAndHardness(this);
    }

    public static <T extends Block> T deduceSoundAndHardness(T block) {
        if (block.soundType == SoundType.STONE) {
            block.soundType = deduceSoundType(block.material);
        }
        if (block.blockHardness == 0) {
            block.blockHardness = deduceHardness(block.material);
            if (block.blockHardness > 0) {
                block.blockResistance = block.blockHardness;
            }
        }
        setFlammability(block);
        return block;
    }

    public static SoundType deduceSoundType(final Material material) {
        if (material == Material.WOOD || material == Material.GOURD) {
            return SoundType.WOOD;
        }
        if (material == Material.EARTH || material == Material.CLAY) {
            return SoundType.GROUND;
        }
        if (material == Material.PLANTS || material == Material.ORGANIC || material == Material.TALL_PLANTS || material == Material.LEAVES || material == Material.SPONGE || material == Material.TNT) {
            return SoundType.PLANT;
        }
        if (material == Material.SEA_GRASS || material == Material.OCEAN_PLANT) {
            return SoundType.WET_GRASS;
        }
        if (material == Material.IRON) {
            return SoundType.METAL;
        }
        if (material == Material.GLASS || material == Material.PORTAL || material == Material.ICE || material == Material.PACKED_ICE || material == Material.REDSTONE_LIGHT) {
            return SoundType.GLASS;
        }
        if (material == Material.WOOL || material == Material.CARPET || material == Material.CACTUS || material == Material.CAKE || material == Material.FIRE) {
            return SoundType.CLOTH;
        }
        if (material == Material.SAND) {
            return SoundType.SAND;
        }
        if (material == Material.SNOW || material == Material.SNOW_BLOCK) {
            return SoundType.SNOW;
        }
        if (material == Material.ANVIL) {
            return SoundType.ANVIL;
        }
        return SoundType.STONE;
    }

    public static float deduceHardness(final Material material) {
        if (material == Material.PLANTS || material == Material.AIR || material == Material.FIRE) {
            return 0;
        }
        if (material == Material.ROCK) {
            return 2.5F;
        }
        if (material == Material.WOOD) {
            return 2;
        }
        if (material == Material.ORGANIC) {
            return 0.6F;
        }
        if (material == Material.SAND || material == Material.EARTH || material == Material.CLAY) {
            return 0.5F;
        }
        if (material == Material.GLASS) {
            return 0.3F;
        }
        if (material == Material.CACTUS) {
            return 0.4F;
        }
        if (material == Material.IRON || material == Material.ANVIL) {
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

    public static void setFlammability(Block block) {
        Material material = block.material;
        FireBlock fire = (FireBlock) Blocks.FIRE;
        if (material == Material.WOOD) {
            if (block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof WoodButtonBlock || block instanceof PressurePlateBlock) {
                return;
            }
            fire.setFireInfo(block, 5, 20);
        } else if (material == Material.PLANTS || material == Material.TALL_PLANTS) {
            if (block instanceof SaplingBlock) {
                return;
            }
            fire.setFireInfo(block, 30, 100);
        } else if (material == Material.CARPET) {
            fire.setFireInfo(block, 60, 20);
        } else if (material == Material.LEAVES) {
            fire.setFireInfo(block, 30, 60);
        } else if (material == Material.WOOL) {
            fire.setFireInfo(block, 30, 60);
        }
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return pickBlock(state, target, world, pos, player);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack pickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack stack = state.getBlock().getItem(world, pos, state);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof BaseTile && !tile.onlyOpsCanSetNbt() && ((BaseTile) tile).persistData) {
            CompoundNBT data = tile.write(new CompoundNBT());
            data.remove("x");
            data.remove("y");
            data.remove("z");
            data.remove("id");
            stack.setTagInfo("BlockEntityTag", data);
        }
        return stack;
    }
}
