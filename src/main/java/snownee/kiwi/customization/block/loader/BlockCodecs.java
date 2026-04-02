package snownee.kiwi.customization.block.loader;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.NotImplementedException;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import snownee.kiwi.customization.block.BasicBlock;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.duck.KBlockProperties;
import snownee.kiwi.util.codec.CustomizationCodecs;

public class BlockCodecs {
	private static final Map<Identifier, MapCodec<Block>> CODECS = Maps.newHashMap();

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

	public static final MapCodec<FenceGateBlock> FENCE_GATE = woodTyped(FenceGateBlock::new);

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

	public static final MapCodec<WallSignBlock> WALL_SIGN = woodTyped(WallSignBlock::new);
	public static final MapCodec<StandingSignBlock> STANDING_SIGN = woodTyped(StandingSignBlock::new);
	public static final MapCodec<WallHangingSignBlock> WALL_HANGING_SIGN = woodTyped(WallHangingSignBlock::new);
	public static final MapCodec<CeilingHangingSignBlock> CEILING_HANGING_SIGN = woodTyped(CeilingHangingSignBlock::new);

	public static final MapCodec<SaplingBlock> SAPLING = RecordCodecBuilder.mapCodec(instance -> instance.group(
			CustomizationCodecs.TREE_GROWER.fieldOf("tree").forGetter(BlockCodecs::notImplemented),
			Block.propertiesCodec()
	).apply(instance, SaplingBlock::new));

	static {
		register("block", BLOCK);
		register("stair", STAIR);
		register("fence_gate", FENCE_GATE);
		register("colored_falling", COLORED_FALLING);
		register("button", BUTTON);
		register("wall_sign", WALL_SIGN);
		register("standing_sign", STANDING_SIGN);
		register("wall_hanging_sign", WALL_HANGING_SIGN);
		register("ceiling_hanging_sign", CEILING_HANGING_SIGN);
		register("sapling", SAPLING);
	}

	public static void register(String key, MapCodec<? extends Block> codec) {
		register(Identifier.withDefaultNamespace(key), codec);
	}

	public static void register(Identifier key, MapCodec<? extends Block> codec) {
		//noinspection unchecked
		CODECS.put(key, (MapCodec<Block>) codec);
	}

	public static MapCodec<Block> get(Identifier key) {
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

	public static <T extends Block> MapCodec<T> woodTyped(BiFunction<WoodType, Block.Properties, T> factory) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				WoodType.CODEC.optionalFieldOf("wood_type", WoodType.OAK).forGetter($ -> WoodType.OAK),
				Block.propertiesCodec()
		).apply(instance, factory));
	}
}
