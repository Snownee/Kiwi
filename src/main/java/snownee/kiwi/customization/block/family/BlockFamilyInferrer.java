package snownee.kiwi.customization.block.family;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.util.resource.OneTimeLoader;

@SuppressWarnings("UnstableApiUsage")
public class BlockFamilyInferrer {
	public static final TagKey<Block> IGNORE = AbstractModule.blockTag("kswitch", "ignore");
	private final List<KHolder<BlockFamily>> families = Lists.newArrayList();
	private final List<PendingGroup> pendingGroups = Lists.newArrayList();
	private final Set<Block> capturedBlocks = Sets.newHashSet();
	private final Map<String, AddonRule> addonRules;
	private final Collection<RecipeHolder<StonecutterRecipe>> stonecutterRecipes;

	public record AddonRule(boolean strict, List<Filter> filters, List<String> templates) {
		static final Codec<AddonRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("strict", false).forGetter(AddonRule::strict),
				Filter.CODEC.listOf().optionalFieldOf("filter", List.of()).forGetter(AddonRule::filters),
				Codec.STRING.listOf().fieldOf("add_block").forGetter(AddonRule::templates)).apply(instance, AddonRule::new));
	}

	public record Filter(String group) {
		static final Codec<Filter> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("group")
				.forGetter(Filter::group)).apply(instance, Filter::new));
	}

	private record PendingGroup(
			String ruleName, String namespace, String path, boolean cascading, List<Holder.Reference<Block>> extraBlocks) {}

	public BlockFamilyInferrer(Map<String, AddonRule> addonRules, Collection<RecipeHolder<StonecutterRecipe>> stonecutterRecipes) {
		this.addonRules = addonRules;
		this.stonecutterRecipes = stonecutterRecipes;
	}

	public static Map<String, AddonRule> loadAddonRules(ResourceManager resourceManager, OneTimeLoader.Context context) {
		Map<Identifier, AddonRule> loaded = OneTimeLoader.load(resourceManager, "kiwi/family_addon", AddonRule.CODEC, context);
		Map<String, AddonRule> result = Maps.newHashMapWithExpectedSize(loaded.size());
		for (Map.Entry<Identifier, AddonRule> entry : loaded.entrySet()) {
			result.put(entry.getKey().getPath(), entry.getValue());
		}
		return result;
	}

	@SuppressWarnings("DuplicateExpressions")
	public Collection<KHolder<BlockFamily>> generate() {
		MutableGraph<Item> graph = GraphBuilder.undirected().allowsSelfLoops(true).build();
		for (RecipeHolder<StonecutterRecipe> recipeHolder : stonecutterRecipes) {
			StonecutterRecipe recipe = recipeHolder.value();
			if (recipe.isSpecial()) {
				continue;
			}
			List<Item> inputs = Lists.newArrayList();
			recipe.input().values.stream().map(Holder::value).forEach(inputs::add);
			if (inputs.isEmpty()) {
				continue;
			}
			Item result = recipe.result().typeHolder().value();
			for (Item input : inputs) {
				if (input == result) {
					continue;
				}
				graph.putEdge(input, result);
			}
		}
		for (List<Item> component : connectedComponents(graph)) {
			if (component.size() < 2) {
				continue;
			}
			List<Holder.Reference<Block>> blocks = Lists.newArrayList();
			for (Item item : component) {
				Optional<Holder.Reference<Block>> block = BuiltInRegistries.BLOCK.get(ResourceKey.create(
						Registries.BLOCK,
						BuiltInRegistries.ITEM.getKey(item)));
				block.ifPresent(blocks::add);
			}
			if (blocks.size() < 2) {
				continue;
			}
			Identifier familyBaseId = BuiltInRegistries.ITEM.getKey(component.getFirst());
			family(familyBaseId, "stonecutting", blocks, true);
		}

		for (Holder<Block> holder : BuiltInRegistries.BLOCK.asHolderIdMap()) {
			if (holder.is(IGNORE)) {
				continue;
			}
			Identifier id = holder.unwrapKey().orElseThrow().identifier();
			String namespace = id.getNamespace();
			String path = id.getPath();
			if (path.startsWith("pink_")) {
				addGroup("colored_prefix", namespace, path.substring(5), false);
			} else if (path.endsWith("_pink")) {
				addGroup("colored_suffix", namespace, path.substring(0, path.length() - 5), false);
			}

			if (holder.is(BlockTags.LOGS)) {
				if (path.endsWith("_log")) {
					addGroup("log", namespace, path.substring(0, path.length() - 4), true);
					continue;
				} else if (path.endsWith("_stem")) {
					addGroup("stem", namespace, path.substring(0, path.length() - 5), true);
					continue;
				}
			}

			if (path.endsWith("_stairs") && holder.is(BlockTags.STAIRS)) {
				addGroup("general", namespace, path.substring(0, path.length() - 7), true);
				continue;
			}
			if (path.endsWith("_slab") && holder.is(BlockTags.SLABS)) {
				addGroup("general", namespace, path.substring(0, path.length() - 5), true);
				continue;
			}
			if (path.endsWith("_block")) {
				addGroup("general", namespace, path.substring(0, path.length() - 6), true);
				continue;
			}
		}

		for (String waxed : List.of("", "waxed_")) {
			for (String variant : List.of("", "exposed_", "weathered_", "oxidized_")) {
				addGroup("copper", "minecraft", waxed + variant + "copper", true);
			}
		}

		for (PendingGroup pending : pendingGroups) {
			AddonRule rule = addonRules.get(pending.ruleName());
			if (rule == null) {
				Kiwi.LOGGER.error("Unknown addon rule: {}", pending.ruleName());
				continue;
			}

			List<Holder.Reference<Block>> blocks = Lists.newArrayList();
			blocks.addAll(pending.extraBlocks());
			for (String template : rule.templates()) {
				String blockIdStr = template.replace("{{namespace}}", pending.namespace()).replace("{{path}}", pending.path());
				Identifier blockId = Identifier.tryParse(blockIdStr);
				if (blockId == null) {
					continue;
				}
				Optional<Holder.Reference<Block>> holder = BuiltInRegistries.BLOCK.get(ResourceKey.create(Registries.BLOCK, blockId));
				holder.ifPresent(blocks::add);
			}

			if (rule.strict() && blocks.size() != rule.templates().size()) {
				continue;
			}

			Identifier familyBaseId = Identifier.fromNamespaceAndPath(pending.namespace(), pending.path());
			family(familyBaseId, rule.filters().getFirst().group(), blocks, pending.cascading());
		}

		return families;
	}

	private static <N> Collection<List<N>> connectedComponents(Graph<N> graph) {
		Collection<List<N>> components = new ArrayList<>();
		Set<N> visited = new HashSet<>();

		// 遍历图中的每一个节点
		for (N node : graph.nodes()) {
			// 如果节点尚未访问过，说明发现了一个新的连通分量
			if (!visited.contains(node)) {
				List<N> component = new ArrayList<>();

				// 使用 BFS 搜索该节点能到达的所有节点
				Deque<N> queue = new ArrayDeque<>();
				queue.add(node);
				visited.add(node);

				while (!queue.isEmpty()) {
					N current = queue.poll();
					component.add(current);

					// 获取相邻节点
					for (N neighbor : graph.adjacentNodes(current)) {
						if (!visited.contains(neighbor)) {
							visited.add(neighbor);
							queue.add(neighbor);
						}
					}
				}

				// 将搜索到的连通分量加入结果集
				components.add(component);
			}
		}

		return components;
	}

	private void addGroup(String ruleName, String namespace, String path, boolean cascading) {
		List<Holder.Reference<Block>> extraBlocks = List.of();
		if (path.endsWith("brick")) {
			Optional<Holder.Reference<Block>> baseBlock = BuiltInRegistries.BLOCK.get(ResourceKey.create(
					Registries.BLOCK,
					Identifier.fromNamespaceAndPath(namespace, path + "s")));
			if (baseBlock.isPresent()) {
				extraBlocks = List.of(baseBlock.get());
			}
		}
		for (PendingGroup pg : pendingGroups) {
			if (pg.ruleName().equals(ruleName) && pg.namespace().equals(namespace) && pg.path().equals(path)) {
				return;
			}
		}
		pendingGroups.add(new PendingGroup(ruleName, namespace, path, cascading, extraBlocks));
	}

	private void family(Identifier id, String desc, List<Holder.Reference<Block>> blocks, boolean cascading) {
		List<ResourceKey<Block>> blockKeys = blocks.stream().filter($ -> !$.is(IGNORE)).map(Holder.Reference::key).toList();
		if (blockKeys.size() < 2) {
			return;
		}
		KHolder<BlockFamily> family = new KHolder<>(
				id.withPrefix("auto/%s/".formatted(desc)), new BlockFamily(
				false,
				blockKeys,
				List.of(),
				Optional.empty(),
				false,
				Optional.empty(),
				1,
				BlockFamily.SwitchAttrs.create(true, cascading, false)));
		families.add(family);
		family.value().blocks().forEach(capturedBlocks::add);
	}
}
