package snownee.kiwi.customization.builder;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.util.resource.OneTimeLoader;

public class BuilderRules {
	private static ImmutableListMultimap<Block, KHolder<BuilderRule>> byBlock = ImmutableListMultimap.of();
	private static ImmutableMap<ResourceLocation, KHolder<BuilderRule>> byId = ImmutableMap.of();

	public static Collection<KHolder<BuilderRule>> find(Block block) {
		return byBlock.get(block);
	}

	public static int reload(ResourceManager resourceManager) {
		Map<ResourceLocation, BuilderRule> families = OneTimeLoader.load(resourceManager, "kiwi/builder_rule", BuilderRule.CODEC);

//		BlockSpread blockSpread = new BlockSpread(BlockSpread.Type.PLANE_XZ, Optional.empty(), FacingLimitation.FrontAndBack, 16);
//		BlockFamily family = Objects.requireNonNull(BlockFamilies.get(new ResourceLocation("xkdeco:black_roof_end")));
//		families.put(new ResourceLocation("test"), new ReplaceBuilderRule(Map.of(family, family), blockSpread));

		byId = families.entrySet()
				.stream()
				.map(e -> new KHolder<>(e.getKey(), e.getValue()))
				.collect(ImmutableMap.toImmutableMap(
						KHolder::key,
						Function.identity()));
		ImmutableListMultimap.Builder<Block, KHolder<BuilderRule>> byBlockBuilder = ImmutableListMultimap.builder();
		for (var holder : byId.values()) {
			holder.value().relatedBlocks().forEach(block -> byBlockBuilder.put(block, holder));
		}
		byBlock = byBlockBuilder.build();
		return byId.size();
	}

	public static BuilderRule get(ResourceLocation id) {
		KHolder<BuilderRule> holder = byId.get(id);
		return holder == null ? null : holder.value();
	}

	public static Collection<KHolder<BuilderRule>> all() {
		return byId.values();
	}
}
