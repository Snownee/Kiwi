package snownee.kiwi.customization.block.family;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.util.KHolder;

public class BlockFamilyInferrer {
	public static final TagKey<Block> IGNORE = AbstractModule.blockTag("kswitch", "ignore");
	private final List<KHolder<BlockFamily>> families = Lists.newArrayList();
	private final Set<Block> capturedBlocks = Sets.newHashSet();
	private final List<String> colorPrefixed = Lists.newArrayList();
	private final List<String> colorSuffixed = Lists.newArrayList();
	private final List<String> logs = List.of("%s_log", "%s_wood", "stripped_%s_log", "stripped_%s_wood");
	private final List<String> netherLogs = List.of("%s_stem", "%s_hyphae", "stripped_%s_stem", "stripped_%s_hyphae");
	private final List<String> general = List.of(
			"%s",
			"%s_stairs",
			"%s_slab",
			"%s_wall",
			"%s_fence",
			"%s_fence_gate",
			"%s_door",
			"%s_trapdoor",
			"%s_button",
			"%s_pressure_plate");
	private final List<String> variants = List.of(
			"%s",
			"chiseled_%s",
			"polished_%s",
			"cut_%s",
			"smooth_%s",
			"cracked_%s",
			"%s_bricks",
			"%s_pillar");

	public BlockFamilyInferrer() {
		for (DyeColor color : DyeColor.values()) {
			colorPrefixed.add(color.getName() + "_%s");
			colorSuffixed.add("%s_" + color.getName());
		}
	}

	public Collection<KHolder<BlockFamily>> generate() {
		List<Holder<Block>> sorted = Lists.newArrayList();
		for (Holder<Block> holder : BuiltInRegistries.BLOCK.asHolderIdMap()) {
			String path = holder.unwrapKey().orElseThrow().location().getPath();
			if (path.startsWith("pink_") || path.endsWith("_pink") || path.endsWith("_log") || path.endsWith("_stem") || path.endsWith(
					"_stairs") || path.endsWith("_slab") || path.startsWith("smooth_")) {
				if (holder.is(IGNORE)) {
					continue;
				}
				sorted.add(holder);
			}
		}
		// make stairs come first
		sorted.sort((a, b) -> {
			String aPath = a.unwrapKey().orElseThrow().location().getPath();
			String bPath = b.unwrapKey().orElseThrow().location().getPath();
			boolean aIsStairs = aPath.endsWith("_stairs");
			boolean bIsStairs = bPath.endsWith("_stairs");
			return Boolean.compare(bIsStairs, aIsStairs);
		});
		for (Holder<Block> holder : sorted) {
			Block block = holder.value();
			if (capturedBlocks.contains(block) || !BlockFamilies.findQuickSwitch(block.asItem(), true).isEmpty()) {
				continue;
			}
			ResourceLocation key = holder.unwrapKey().orElseThrow().location();
			String path = key.getPath();
			boolean captured = false;
			if (path.startsWith("pink_")) {
				ResourceLocation id = key.withPath(path.substring(5));
				generateColored(id, colorPrefixed);
				captured = true;
			} else if (path.endsWith("_pink")) {
				ResourceLocation id = key.withPath(path.substring(0, path.length() - 5));
				generateColored(id, colorSuffixed);
				captured = true;
			}
			if (path.endsWith("_log")) {
				if (holder.is(BlockTags.LOGS)) {
					ResourceLocation id = key.withPath(path.substring(0, path.length() - 4));
					fromTemplates(id, "logs", logs, true);
				}
				continue;
			} else if (path.endsWith("_stem")) {
				if (holder.is(BlockTags.LOGS)) {
					ResourceLocation id = key.withPath(path.substring(0, path.length() - 5));
					fromTemplates(id, "logs", netherLogs, true);
				}
				continue;
			}
			if (path.endsWith("_stairs")) {
				if (!(block instanceof StairBlock stairBlock)) {
					continue;
				}
				if (stairBlock.baseState.isAir()) {
					continue;
				}
				ResourceLocation id = key.withPath(path.substring(0, path.length() - 7));
				List<Holder.Reference<Block>> blocks = collectBlocks(id, general);
				//noinspection deprecation
				Holder.Reference<Block> baseHolder = stairBlock.baseState.getBlock().builtInRegistryHolder();
				id = baseHolder.key().location();
				blocks.add(0, baseHolder);
				blocks.addAll(collectBlocks(id, variants));
				if (id.getPath().endsWith("_block")) {
					ResourceLocation altId = id.withPath(id.getPath().substring(0, id.getPath().length() - 6));
					blocks.addAll(collectBlocks(altId, variants));
				}
				family(id, "variants", blocks.stream().distinct().toList(), true);
				continue;
			}
			if (path.endsWith("_slab")) {
				ResourceLocation id = key.withPath(path.substring(0, path.length() - 5));
				fromTemplates(id, "general", general, true);
				captured = true;
			} else if (path.startsWith("smooth_")) {
				ResourceLocation id = key.withPath(path.substring(7));
				fromTemplates(id, "variants", variants, true);
				captured = true;
			}
			if (!captured) {
				throw new IllegalStateException("Unrecognized block: " + holder.value());
			}
		}
		List<String> normalCopperTemplate = List.of("%s_block", "cut_%s", "chiseled_%s", "%s_grate");
		List<String> otherCopperTemplate = List.of("%s", "cut_%s", "chiseled_%s", "%s_grate");
		ResourceLocation copperId = new ResourceLocation("copper");
		for (String waxed : List.of("", "waxed_")) {
			for (String variant : List.of("", "exposed_", "weathered_", "oxidized_")) {
				List<String> template = variant.isEmpty() ? normalCopperTemplate : otherCopperTemplate;
				template = template.stream().map($ -> waxed + variant + $).toList();
				fromTemplates(copperId, waxed + variant + "copper", template, true);
			}
		}
		return families;
	}

	private List<Holder.Reference<Block>> collectBlocks(ResourceLocation id, List<String> templates) {
		List<Holder.Reference<Block>> blocks = Lists.newArrayList();
		for (String template : templates) {
			ResourceLocation blockId = id.withPath(String.format(template, id.getPath()));
			Optional<Holder.Reference<Block>> holder = BuiltInRegistries.BLOCK.getHolder(ResourceKey.create(Registries.BLOCK, blockId));
			holder.ifPresent(blocks::add);
		}
		return blocks;
	}

	private void fromTemplates(ResourceLocation id, String desc, List<String> templates, boolean cascading) {
		List<Holder.Reference<Block>> blocks = collectBlocks(id, templates);
		if (blocks.size() < 2) {
			return;
		}
		family(id, desc, blocks, cascading);
	}

	private void generateColored(ResourceLocation id, List<String> templates) {
		List<Holder.Reference<Block>> blocks = collectBlocks(id, templates);
		if (blocks.size() != templates.size()) {
			return;
		}
		family(id, "colored", blocks, false);
	}

	private void family(ResourceLocation id, String desc, List<Holder.Reference<Block>> blocks, boolean cascading) {
		List<ResourceKey<Block>> blockKeys = blocks.stream().filter($ -> !$.is(IGNORE)).map(Holder.Reference::key).toList();
		KHolder<BlockFamily> family = new KHolder<>(
				id.withPrefix("auto/%s/".formatted(desc)),
				new BlockFamily(
						false,
						blockKeys,
						List.of(),
						List.of(),
						false,
						Optional.empty(),
						1,
						BlockFamily.SwitchAttrs.create(true, cascading, false)));
		families.add(family);
		family.value().blocks().forEach(capturedBlocks::add);
	}
}
