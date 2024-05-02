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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import snownee.kiwi.Kiwi;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.KHolder;

public class BlockFamilyInferrer {
	private final List<KHolder<BlockFamily>> families = Lists.newArrayList();
	private final Set<Block> capturedBlocks = Sets.newHashSet();
	private final List<String> colorPrefixed = Lists.newArrayList();
	private final List<String> colorSuffixed = Lists.newArrayList();
	private final List<String> logs = List.of("%s_log", "%s_wood", "stripped_%s_log", "stripped_%s_wood");
	private final List<String> general = List.of("%s_stairs", "%s_slab", "%s_wall", "%s_fence", "%s_fence_gate");
	private final List<String> variants = List.of(
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
		for (Holder<Block> holder : BuiltInRegistries.BLOCK.asHolderIdMap()) {
			Block block = holder.value();
			if (capturedBlocks.contains(block) || !BlockFamilies.findQuickSwitch(block.asItem()).isEmpty()) {
				continue;
			}
			ResourceLocation key = holder.unwrapKey().orElseThrow().location();
			if (key.getPath().startsWith("pink_")) {
				ResourceLocation id = key.withPath(key.getPath().substring(5));
				generateColored(id, colorPrefixed);
			} else if (key.getPath().endsWith("_pink")) {
				ResourceLocation id = key.withPath(key.getPath().substring(0, key.getPath().length() - 5));
				generateColored(id, colorSuffixed);
			}
			if (key.getPath().endsWith("_log") && holder.is(BlockTags.LOGS)) {
				ResourceLocation id = key.withPath(key.getPath().substring(0, key.getPath().length() - 4));
				fromTemplates(id, logs, true);
				continue;
			}
			if (key.getPath().endsWith("_stairs") && block instanceof StairBlock stairBlock) {
				if (stairBlock.baseState.isAir()) {
					continue;
				}
				ResourceLocation id = key.withPath(key.getPath().substring(0, key.getPath().length() - 7));
				List<Holder<Block>> blocks = collectBlocks(id, general);
				//noinspection deprecation
				Holder.Reference<Block> baseHolder = stairBlock.baseState.getBlock().builtInRegistryHolder();
				id = baseHolder.key().location();
				blocks.add(0, baseHolder);
				blocks.addAll(collectBlocks(id, variants));
				if (id.getPath().endsWith("_block")) {
					ResourceLocation altId = id.withPath(id.getPath().substring(0, id.getPath().length() - 6));
					blocks.addAll(collectBlocks(altId, variants));
				}
				family(id, blocks, true);
			}
		}
		if (!Platform.isProduction()) {
			for (KHolder<BlockFamily> family : families) {
				Kiwi.LOGGER.info(family.key().toString() + ":");
				for (Holder<Block> holder : family.value().blockHolders()) {
					Kiwi.LOGGER.info("  - " + holder.unwrapKey().orElseThrow());
				}
			}
		}
		return families;
	}

	private List<Holder<Block>> collectBlocks(ResourceLocation id, List<String> templates) {
		List<Holder<Block>> blocks = Lists.newArrayList();
		for (String template : templates) {
			ResourceLocation blockId = id.withPath(String.format(template, id.getPath()));
			Optional<Holder.Reference<Block>> holder = BuiltInRegistries.BLOCK.getHolder(ResourceKey.create(Registries.BLOCK, blockId));
			holder.ifPresent(blocks::add);
		}
		return blocks;
	}

	private void fromTemplates(ResourceLocation id, List<String> templates, boolean cascading) {
		List<Holder<Block>> blocks = collectBlocks(id, templates);
		if (blocks.size() < 2) {
			return;
		}
		family(id, blocks, cascading);
	}

	private void generateColored(ResourceLocation id, List<String> templates) {
		List<Holder<Block>> blocks = collectBlocks(id, templates);
		if (blocks.size() != templates.size()) {
			return;
		}
		family(id, blocks, false);
	}

	private void family(ResourceLocation id, List<Holder<Block>> blocks, boolean cascading) {
		KHolder<BlockFamily> family = new KHolder<>(
				id.withPrefix("auto/"),
				new BlockFamily(blocks, List.of(), List.of(), false, Items.AIR, 1, true, cascading));
		families.add(family);
		family.value().blocks().forEach(capturedBlocks::add);
	}
}
