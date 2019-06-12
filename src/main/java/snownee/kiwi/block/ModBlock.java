package snownee.kiwi.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class ModBlock extends Block
{
    public ModBlock(Block.Properties builder)
    {
        super(builder);
        deduceSoundAndHardness(this);
    }

    public static Block deduceSoundAndHardness(Block block)
    {
        if (block.soundType == SoundType.STONE)
        {
            block.soundType = deduceSoundType(block.material);
        }
        if (block.blockHardness == 0)
        {
            block.blockHardness = deduceHardness(block.material);
            if (block.blockHardness > 0)
            {
                block.blockResistance = block.blockHardness;
            }
        }
        return block;
    }

    public static SoundType deduceSoundType(final Material material)
    {
        if (material == Material.WOOD || material == Material.GOURD)
        {
            return SoundType.WOOD;
        }
        if (material == Material.EARTH || material == Material.CLAY)
        {
            return SoundType.GROUND;
        }
        if (material == Material.PLANTS || material == Material.ORGANIC || material == Material.TALL_PLANTS || material == Material.LEAVES || material == Material.SPONGE || material == Material.TNT)
        {
            return SoundType.PLANT;
        }
        if (material == Material.SEA_GRASS || material == Material.OCEAN_PLANT)
        {
            return SoundType.WET_GRASS;
        }
        if (material == Material.IRON)
        {
            return SoundType.METAL;
        }
        if (material == Material.GLASS || material == Material.PORTAL || material == Material.ICE || material == Material.PACKED_ICE || material == Material.REDSTONE_LIGHT)
        {
            return SoundType.GLASS;
        }
        if (material == Material.WOOL || material == Material.CARPET || material == Material.CACTUS || material == Material.CAKE || material == Material.FIRE)
        {
            return SoundType.CLOTH;
        }
        if (material == Material.SAND)
        {
            return SoundType.SAND;
        }
        if (material == Material.SNOW || material == Material.SNOW_BLOCK)
        {
            return SoundType.SNOW;
        }
        if (material == Material.ANVIL)
        {
            return SoundType.ANVIL;
        }
        return SoundType.STONE;
    }

    public static float deduceHardness(final Material material)
    {
        if (material == Material.PLANTS || material == Material.AIR || material == Material.FIRE)
        {
            return 0;
        }
        if (material == Material.ROCK)
        {
            return 2.5F;
        }
        if (material == Material.WOOD)
        {
            return 2;
        }
        if (material == Material.ORGANIC)
        {
            return 0.6F;
        }
        if (material == Material.SAND || material == Material.EARTH || material == Material.CLAY)
        {
            return 0.5F;
        }
        if (material == Material.GLASS)
        {
            return 0.3F;
        }
        if (material == Material.CACTUS)
        {
            return 0.4F;
        }
        if (material == Material.IRON || material == Material.ANVIL)
        {
            return 5;
        }
        if (material == Material.WEB)
        {
            return 4;
        }
        if (material == Material.WOOL)
        {
            return 0.8F;
        }
        if (material == Material.WATER || material == Material.LAVA)
        {
            return 100;
        }
        return 1;
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face)
    {
        if (state.has(BlockStateProperties.WATERLOGGED))
        {
            return 0;
        }
        if (material == Material.WOOD)
        {
            return 20;
        }
        if (material == Material.PLANTS)
        {
            return 100;
        }
        if (material == Material.CARPET)
        {
            return 20;
        }
        if (material == Material.TALL_PLANTS)
        {
            return 100;
        }
        if (material == Material.LEAVES)
        {
            return 60;
        }
        if (material == Material.WOOL)
        {
            return 60;
        }
        return 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face)
    {
        if (state.has(BlockStateProperties.WATERLOGGED))
        {
            return 0;
        }
        if (material == Material.WOOD)
        {
            return 5;
        }
        if (material == Material.PLANTS)
        {
            return 60;
        }
        if (material == Material.CARPET)
        {
            return 60;
        }
        if (material == Material.TALL_PLANTS)
        {
            return 15;
        }
        if (material == Material.LEAVES)
        {
            return 30;
        }
        if (material == Material.WOOL)
        {
            return 30;
        }
        return 0;
    }
}
