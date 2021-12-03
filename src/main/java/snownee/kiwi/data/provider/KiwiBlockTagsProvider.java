package snownee.kiwi.data.provider;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.data.ExistingFileHelper;
import snownee.kiwi.mixin.BlockAccessor;

public abstract class KiwiBlockTagsProvider extends BlockTagsProvider {

	public KiwiBlockTagsProvider(DataGenerator pGenerator, String modId, ExistingFileHelper existingFileHelper) {
		super(pGenerator, modId, existingFileHelper);
	}

	public void processTools(Block block) {
		Material material = ((BlockAccessor) block).getMaterial();
		if (material == Material.LEAVES) {
			tag(BlockTags.MINEABLE_WITH_HOE).add(block);
		} else if (material == Material.WOOD || material == Material.NETHER_WOOD || material == Material.PLANT || material == Material.REPLACEABLE_PLANT || material == Material.VEGETABLE) {
			tag(BlockTags.MINEABLE_WITH_AXE).add(block);
		} else if (material == Material.STONE || material == Material.METAL || material == Material.HEAVY_METAL) {
			tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
		} else if (material == Material.SAND || material == Material.CLAY || material == Material.DIRT || material == Material.SNOW) {
			tag(BlockTags.MINEABLE_WITH_SHOVEL).add(block);
		}
	}

	public TagsProvider.TagAppender<Block> tag(String name) {
		return tag(BlockTags.createOptional(new ResourceLocation(modId, name)));
	}
}
