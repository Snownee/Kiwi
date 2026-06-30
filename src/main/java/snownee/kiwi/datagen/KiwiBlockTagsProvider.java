package snownee.kiwi.datagen;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.util.GameObjectLookup;

public abstract class KiwiBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {
	protected final FabricPackOutput output;

	public KiwiBlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
		super(output, registryLookupFuture);
		this.output = output;
	}

	@Override
	protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
		return lookupProvider.thenApply(registries -> {
			builders.clear();
			addGenericTags(registries);
			addTags(registries);
			return registries;
		});
	}

	protected void addGenericTags(HolderLookup.Provider registries) {
		List<Block> blocks = GameObjectLookup.all(registries, Registries.BLOCK, output.getModId())
				.filter(b -> b.defaultBlockState().canBeReplaced()).toList();
		if (!blocks.isEmpty()) {
			valueLookupBuilder(BlockTags.REPLACEABLE).addAll(blocks);
		}
	}
}