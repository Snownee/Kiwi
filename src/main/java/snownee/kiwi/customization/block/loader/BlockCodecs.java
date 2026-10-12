package snownee.kiwi.customization.block.loader;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.util.valueproviders.ConstantInt;
import snownee.kiwi.customization.InjectedCodec;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
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
import snownee.kiwi.util.codec.KCodecs;

public class BlockCodecs {
	private static final Map<Identifier, MapCodec<Block>> CODECS = Maps.newHashMap();

	public static final String BLOCK_PROPERTIES_KEY = "properties";
	private static final Codec<BlockBehaviour.Properties> BLOCK_PROPERTIES = new InjectedCodec<>(
			MapCodec.unitCodec(BlockBehaviour.Properties::of), BuiltInBlockTemplate.PROPERTIES_INJECTOR);

	public static <B extends Block> RecordCodecBuilder<B, BlockBehaviour.Properties> propertiesCodec() {
		return BLOCK_PROPERTIES.fieldOf(BLOCK_PROPERTIES_KEY).forGetter(BlockBehaviour::properties);
	}

	public static <B extends Block> MapCodec<B> simpleCodec(Function<BlockBehaviour.Properties, B> factory) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(propertiesCodec()).apply(instance, factory));
	}

	public static <B extends Block> MapCodec<B> blockSetTyped(BiFunction<BlockSetType, BlockBehaviour.Properties, B> factory) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				BlockSetType.CODEC.fieldOf("block_set_type").forGetter(KCodecs.unsupportedGetter()),
				propertiesCodec()).apply(instance, factory));
	}

	public static final Function<BlockBehaviour.Properties, Block> SIMPLE_BLOCK_FACTORY = properties -> {
		KBlockSettings settings = ((KBlockProperties) properties).kiwi$getSettings();
		if (settings != null && settings.hasComponent(KBlockComponents.WATER_LOGGABLE.getOrCreate())) {
			return new BasicBlock(properties);
		} else {
			return new Block(properties);
		}
	};

	public static final MapCodec<StairBlock> STAIR = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockState.CODEC.optionalFieldOf("base_state", Blocks.AIR.defaultBlockState())
					.forGetter(block -> {throw new UnsupportedOperationException();}),
			propertiesCodec()
	).apply(instance, StairBlock::new));

	public static final MapCodec<ColoredFallingBlock> COLORED_FALLING = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ColorRGBA.CODEC.optionalFieldOf("falling_dust_color", new ColorRGBA(14406560)).forGetter($ -> new ColorRGBA(14406560)),
			propertiesCodec()
	).apply(instance, ColoredFallingBlock::new));

	public static final MapCodec<ButtonBlock> BUTTON = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockSetType.CODEC.fieldOf("block_set_type").forGetter(KCodecs.unsupportedGetter()),
			Codec.intRange(1, 1024).optionalFieldOf("ticks_to_stay_pressed").forGetter(KCodecs.unsupportedGetter()),
			propertiesCodec()
	).apply(
			instance,
			(blockSetType, ticksToStayPressed, properties) -> {
				return new ButtonBlock(blockSetType, ticksToStayPressed.orElse(blockSetType.canOpenByHand() ? 30 : 20), properties);
			}));

	public static final MapCodec<SaplingBlock> SAPLING = RecordCodecBuilder.mapCodec(instance -> instance.group(
			CustomizationCodecs.TREE_GROWER.fieldOf("tree").forGetter(KCodecs.unsupportedGetter()),
			propertiesCodec()
	).apply(instance, SaplingBlock::new));

	static {
		register("block", simpleCodec(SIMPLE_BLOCK_FACTORY));
		register("stair", STAIR);
		// Vanilla removed the block-type codec registry in 26.3; retain the bundled templates here.
		register("fence", simpleCodec(FenceBlock::new));
		register("wall", simpleCodec(WallBlock::new));
		register("slab", simpleCodec(SlabBlock::new));
		register("rotated_pillar", simpleCodec(RotatedPillarBlock::new));
		register("iron_bars", simpleCodec(IronBarsBlock::new));
		register("door", blockSetTyped(DoorBlock::new));
		register("trapdoor", blockSetTyped(TrapDoorBlock::new));
		register("pressure_plate", blockSetTyped(PressurePlateBlock::new));
		register("drop_experience", RecordCodecBuilder.<DropExperienceBlock>mapCodec(instance -> instance.group(
				IntProviders.NON_NEGATIVE_CODEC.optionalFieldOf("experience", ConstantInt.of(0)).forGetter(KCodecs.unsupportedGetter()),
				propertiesCodec()).apply(instance, DropExperienceBlock::new)));
		register("fence_gate", woodTyped(FenceGateBlock::new));
		register("colored_falling", COLORED_FALLING);
		register("button", BUTTON);
		register("wall_sign", woodTyped(WallSignBlock::new));
		register("standing_sign", woodTyped(StandingSignBlock::new));
		register("wall_hanging_sign", woodTyped(WallHangingSignBlock::new));
		register("ceiling_hanging_sign", woodTyped(CeilingHangingSignBlock::new));
		register("sapling", SAPLING);
		register(
				"tinted_particle_leaves", RecordCodecBuilder.<TintedParticleLeavesBlock>mapCodec(instance -> instance.group(
						Codec.FLOAT.optionalFieldOf("leaf_particle_chance", 0.01F).forGetter(KCodecs.unsupportedGetter()),
						propertiesCodec()
				).apply(instance, TintedParticleLeavesBlock::new)));
		register(
				"untinted_particle_leaves", RecordCodecBuilder.<UntintedParticleLeavesBlock>mapCodec(instance -> instance.group(
						Codec.FLOAT.optionalFieldOf("leaf_particle_chance", 0.01F).forGetter(KCodecs.unsupportedGetter()),
						ParticleTypes.CODEC.fieldOf("leaf_particle").forGetter(KCodecs.unsupportedGetter()),
						AmbientLeavesBlockSoundPlayer.CODEC.optionalFieldOf("ambient_sound", AmbientLeavesBlockSoundPlayer.noAmbientSound()).forGetter(KCodecs.unsupportedGetter()),
						propertiesCodec()
				).apply(instance, UntintedParticleLeavesBlock::new)));
	}

	public static void register(String key, MapCodec<? extends Block> codec) {
		register(Identifier.withDefaultNamespace(key), codec);
	}

	public static void register(Identifier key, MapCodec<? extends Block> codec) {
		//noinspection unchecked
		CODECS.put(key, (MapCodec<Block>) codec);
	}

	public static MapCodec<Block> get(Identifier key) {
		return Objects.requireNonNull(CODECS.get(key), () -> "No block template codec registered for " + key);
	}

	public static <T extends Block> MapCodec<T> woodTyped(BiFunction<WoodType, Block.Properties, T> factory) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				WoodType.CODEC.optionalFieldOf("wood_type", WoodType.OAK).forGetter($ -> WoodType.OAK),
				propertiesCodec()
		).apply(instance, factory));
	}
}
