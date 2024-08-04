package snownee.kiwi.customization.item.loader;

import java.util.Optional;
import java.util.function.BiFunction;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ScaffoldingBlockItem;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class BlockItemTemplate extends KItemTemplate {
	private final Optional<ResourceLocation> block;
	private final String clazz;
	private BiFunction<Block, Item.Properties, Item> constructor;

	public BlockItemTemplate(
			Optional<ItemDefinitionProperties> properties,
			Optional<ResourceLocation> block,
			String clazz) {
		super(properties);
		this.block = block;
		this.clazz = clazz;
	}

	public static Codec<BlockItemTemplate> directCodec() {
		return RecordCodecBuilder.create(instance -> instance.group(
				ItemDefinitionProperties.mapCodecField().forGetter(BlockItemTemplate::properties),
				ResourceLocation.CODEC.optionalFieldOf("block").forGetter(BlockItemTemplate::block),
				Codec.STRING.optionalFieldOf("class", "").forGetter(BlockItemTemplate::clazz)
		).apply(instance, BlockItemTemplate::new));
	}

	@Override
	public Type<?> type() {
		return KItemTemplates.BLOCK.getOrCreate();
	}

	@Override
	public void resolve(ResourceLocation key) {
		if (clazz.isEmpty()) {
			constructor = (block, properties) -> {
				if (block instanceof DoorBlock || block instanceof DoublePlantBlock) {
					return new DoubleHighBlockItem(block, properties);
				} else if (block instanceof BedBlock) {
					return new BedItem(block, properties);
				} else if (block instanceof ScaffoldingBlock) {
					return new ScaffoldingBlockItem(block, properties);
				}
				return new BlockItem(block, properties);
			};
			return;
		}
		try {
			Class<?> clazz = Class.forName(this.clazz);
			this.constructor = (block, properties) -> {
				try {
					return (Item) clazz.getConstructor(Block.class, Item.Properties.class).newInstance(block, properties);
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			};
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Item createItem(ResourceLocation id, Item.Properties properties, JsonObject json) {
		Block block = BuiltInRegistries.BLOCK.get(this.block.orElse(id));
		Preconditions.checkState(block != Blocks.AIR, "Block %s not found", this.block);
		return constructor.apply(block, properties);
	}

	public Optional<ResourceLocation> block() {
		return block;
	}

	public String clazz() {
		return clazz;
	}

	@Override
	public String toString() {
		return "BlockItemTemplate[" + "properties=" + properties + ", " + "block=" + block + ']';
	}

}
