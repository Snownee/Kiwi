package snownee.kiwi.customization.builder;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.util.resource.OneTimeLoader;

public class BuilderRules {
	private static ImmutableListMultimap<Block, KHolder<BuilderRule>> byBlock = ImmutableListMultimap.of();
	private static ImmutableMap<Identifier, KHolder<BuilderRule>> byId = ImmutableMap.of();

	public static Collection<KHolder<BuilderRule>> find(Block block) {
		return byBlock.get(block);
	}

	public static int reload(ResourceManager resourceManager, OneTimeLoader.Context context) {
		Map<Identifier, BuilderRule> families = OneTimeLoader.load(resourceManager, "kiwi/builder_rule", BuilderRule.CODEC, context);

//		if (!Platform.isProduction()) {
//			BlockSpread blockSpread = new BlockSpread(BlockSpread.Type.PLANE_Y, FacingLimitation.FrontAndBack, 16);
//			BlockFamily family = Objects.requireNonNull(BlockFamilies.get(Identifier.parse("test:wool")));
//			families.put(Identifier.parse("wool"), new ReplaceInHandRule(Map.of(family, family), blockSpread));
//
//			blockSpread = new BlockSpread(BlockSpread.Type.PLANE_Y, FacingLimitation.None, 16);
//			family = Objects.requireNonNull(BlockFamilies.get(Identifier.parse("test:fence_gate")));
//			families.put(Identifier.parse("fence_gate"), new CyclePropertyRule(Map.of(family, "open"), blockSpread));
//		}

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

	@Nullable
	public static BuilderRule get(Identifier id) {
		KHolder<BuilderRule> holder = byId.get(id);
		return holder == null ? null : holder.value();
	}

	public static Collection<KHolder<BuilderRule>> all() {
		return byId.values();
	}
}
