package snownee.kiwi.customization.block.loader;

import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.NotImplementedException;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import snownee.kiwi.customization.block.BasicBlock;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.duck.KBlockProperties;

public class BlockCodecs {
	private static final Map<ResourceLocation, MapCodec<Block>> CODECS = Maps.newHashMap();

	public static final String BLOCK_PROPERTIES_KEY = "properties";

	public static final Function<BlockBehaviour.Properties, Block> SIMPLE_BLOCK_FACTORY = properties -> {
		KBlockSettings settings = ((KBlockProperties) properties).kiwi$getSettings();
		if (settings != null && settings.hasComponent(KBlockComponents.WATER_LOGGABLE.getOrCreate())) {
			return new BasicBlock(properties);
		} else {
			return new Block(properties);
		}
	};

	public static final MapCodec<Block> BLOCK = Block.simpleCodec(SIMPLE_BLOCK_FACTORY);

	public static final MapCodec<StairBlock> STAIR = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockState.CODEC.optionalFieldOf("base_state", Blocks.AIR.defaultBlockState())
					.forGetter(block -> {throw new UnsupportedOperationException();}),
			Block.propertiesCodec()
	).apply(instance, StairBlock::new));

	public static final MapCodec<FenceGateBlock> FENCE_GATE = RecordCodecBuilder.mapCodec(instance -> instance.group(
			WoodType.CODEC.optionalFieldOf("wood_type", WoodType.OAK).forGetter($ -> WoodType.OAK),
			Block.propertiesCodec()
	).apply(instance, FenceGateBlock::new));

	public static final MapCodec<ColoredFallingBlock> COLORED_FALLING = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ColorRGBA.CODEC.optionalFieldOf("falling_dust_color", new ColorRGBA(14406560)).forGetter($ -> new ColorRGBA(14406560)),
			Block.propertiesCodec()
	).apply(instance, ColoredFallingBlock::new));

	public static final MapCodec<ButtonBlock> BUTTON = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockSetType.CODEC.fieldOf("block_set_type").forGetter(BlockCodecs::notImplemented),
			Codec.intRange(1, 1024).optionalFieldOf("ticks_to_stay_pressed").forGetter(BlockCodecs::notImplemented),
			Block.propertiesCodec()
	).apply(
			instance,
			(blockSetType, ticksToStayPressed, properties) -> {
				return new ButtonBlock(blockSetType, ticksToStayPressed.orElse(blockSetType.canOpenByHand() ? 30 : 20), properties);
			}));

	static {
		register(ResourceLocation.withDefaultNamespace("block"), BLOCK);
		register(ResourceLocation.withDefaultNamespace("stair"), STAIR);
		register(ResourceLocation.withDefaultNamespace("fence_gate"), FENCE_GATE);
		register(ResourceLocation.withDefaultNamespace("colored_falling"), COLORED_FALLING);
		register(ResourceLocation.withDefaultNamespace("button"), BUTTON);
	}

	public static void register(ResourceLocation key, MapCodec<? extends Block> codec) {
		//noinspection unchecked
		CODECS.put(key, (MapCodec<Block>) codec);
	}

	public static MapCodec<Block> get(ResourceLocation key) {
		MapCodec<Block> codec = CODECS.get(key);
		if (codec != null) {
			return codec;
		}
		//noinspection unchecked
		return (MapCodec<Block>) BuiltInRegistries.BLOCK_TYPE.get(key);
	}

	public static <O, A> A notImplemented(O block) {
		throw new NotImplementedException();
	}
}
