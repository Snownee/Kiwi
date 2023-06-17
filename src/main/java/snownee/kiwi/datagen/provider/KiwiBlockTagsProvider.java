package snownee.kiwi.datagen.provider;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class KiwiBlockTagsProvider extends BlockTagsProvider {

	protected final TagsProviderHelper<Block> helper;

	public KiwiBlockTagsProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, String modId, ExistingFileHelper existingFileHelper) {
		super(packOutput, lookupProvider, modId, existingFileHelper);
		helper = new TagsProviderHelper<>(this);
	}

	/*	public void processTools(OptionalEntry<Block> entry) {
		processTools(entry, true);
	}

	public boolean processTools(OptionalEntry<Block> entry, boolean addParentInstead) {
		Block block = entry.object();
		Material material = ((BlockAccess) block).getMaterial();
		if (material == Material.LEAVES) {
			helper.add(BlockTags.MINEABLE_WITH_HOE, entry);
			return true;
		} else if (material == Material.WOOD || material == Material.NETHER_WOOD) {
			if (addParentInstead) {
				if (block instanceof WallSignBlock) {
					helper.add(BlockTags.WALL_SIGNS, entry);
					return true;
				}
				if (block instanceof StandingSignBlock) {
					helper.add(BlockTags.STANDING_SIGNS, entry);
					return true;
				}
				if (block instanceof ButtonBlock) {
					helper.add(BlockTags.WOODEN_BUTTONS, entry);
					return true;
				}
				if (block instanceof DoorBlock) {
					helper.add(BlockTags.WOODEN_DOORS, entry);
					return true;
				}
				if (block instanceof FenceBlock) {
					helper.add(BlockTags.WOODEN_FENCES, entry);
					return true;
				}
				if (block instanceof StairBlock) {
					helper.add(BlockTags.WOODEN_STAIRS, entry);
					return true;
				}
				if (block instanceof SlabBlock) {
					helper.add(BlockTags.WOODEN_SLABS, entry);
					return true;
				}
				if (block instanceof PressurePlateBlock) {
					helper.add(BlockTags.WOODEN_PRESSURE_PLATES, entry);
					return true;
				}
				if (block instanceof TrapDoorBlock) {
					helper.add(BlockTags.WOODEN_TRAPDOORS, entry);
					return true;
				}
			}
			helper.add(BlockTags.MINEABLE_WITH_AXE, entry);
		} else if (material == Material.PLANT || material == Material.REPLACEABLE_PLANT || material == Material.VEGETABLE) {
			if (addParentInstead) {
				if (block instanceof SaplingBlock) {
					helper.add(BlockTags.SAPLINGS, entry);
					return true;
				}
			}
			helper.add(BlockTags.MINEABLE_WITH_AXE, entry);
			return true;
		} else if (material == Material.STONE || material == Material.METAL || material == Material.HEAVY_METAL) {
			if (addParentInstead) {
				if (block instanceof WallBlock) {
					helper.add(BlockTags.WALLS, entry);
					return true;
				}
				if (block instanceof BaseRailBlock) {
					helper.add(BlockTags.RAILS, entry);
					return true;
				}
			}
			helper.add(BlockTags.MINEABLE_WITH_PICKAXE, entry);
			return true;
		} else if (material == Material.SAND || material == Material.CLAY || material == Material.DIRT || material == Material.SNOW) {
			helper.add(BlockTags.MINEABLE_WITH_SHOVEL, entry);
			return true;
		}
		return false;
	}*/

	public TagsProvider.TagAppender<Block> tag(String name) {
		return tag(BlockTags.create(new ResourceLocation(modId, name)));
	}
}
