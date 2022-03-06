package snownee.kiwi.datagen.provider;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WoodButtonBlock;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.data.ExistingFileHelper;
import snownee.kiwi.Kiwi;
import snownee.kiwi.mixin.BlockAccess;

public abstract class KiwiBlockTagsProvider extends BlockTagsProvider {

	public KiwiBlockTagsProvider(DataGenerator pGenerator, String modId, ExistingFileHelper existingFileHelper) {
		super(pGenerator, modId, existingFileHelper);
	}

	public void processTools(Block block) {
		processTools(block, true);
	}

	public void processTools(Block block, boolean addParentInstead) {
		if (block instanceof BannerBlock) {
			tag(BlockTags.BANNERS).add(block);
			return;
		}
		Material material = ((BlockAccess) block).getMaterial();
		if (material == Material.LEAVES) {
			tag(BlockTags.MINEABLE_WITH_HOE).add(block);
		} else if (material == Material.WOOD || material == Material.NETHER_WOOD) {
			if (addParentInstead) {
				if (block instanceof WallSignBlock) {
					tag(BlockTags.WALL_SIGNS).add(block);
					return;
				}
				if (block instanceof StandingSignBlock) {
					tag(BlockTags.STANDING_SIGNS).add(block);
					return;
				}
				if (block instanceof WoodButtonBlock) {
					tag(BlockTags.WOODEN_BUTTONS).add(block);
					return;
				}
				if (block instanceof DoorBlock) {
					tag(BlockTags.WOODEN_DOORS).add(block);
					return;
				}
				if (block instanceof FenceBlock) {
					tag(BlockTags.WOODEN_FENCES).add(block);
					return;
				}
				if (block instanceof StairBlock) {
					tag(BlockTags.WOODEN_STAIRS).add(block);
					return;
				}
				if (block instanceof SlabBlock) {
					tag(BlockTags.WOODEN_SLABS).add(block);
					return;
				}
				if (block instanceof PressurePlateBlock) {
					tag(BlockTags.WOODEN_PRESSURE_PLATES).add(block);
					return;
				}
				if (block instanceof TrapDoorBlock) {
					tag(BlockTags.WOODEN_TRAPDOORS).add(block);
					return;
				}
			}
			tag(BlockTags.MINEABLE_WITH_AXE).add(block);
		} else if (material == Material.PLANT || material == Material.REPLACEABLE_PLANT || material == Material.VEGETABLE) {
			if (addParentInstead) {
				if (block instanceof SaplingBlock) {
					tag(BlockTags.SAPLINGS).add(block);
					return;
				}
			}
			tag(BlockTags.MINEABLE_WITH_AXE).add(block);
		} else if (material == Material.STONE || material == Material.METAL || material == Material.HEAVY_METAL) {
			if (addParentInstead) {
				if (block instanceof WallBlock) {
					tag(BlockTags.WALLS).add(block);
					return;
				}
				if (block instanceof BaseRailBlock) {
					tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
					return;
				}
			}
			tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
		} else if (material == Material.SAND || material == Material.CLAY || material == Material.DIRT || material == Material.SNOW) {
			tag(BlockTags.MINEABLE_WITH_SHOVEL).add(block);
		} else {
			Kiwi.logger.info("Unhandled block: {}", block);
		}
	}

	public TagsProvider.TagAppender<Block> tag(String name) {
		return tag(BlockTags.create(new ResourceLocation(modId, name)));
	}
}
