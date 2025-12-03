package snownee.kiwi.customization.block.loader;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.NotImplementedException;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;
import net.minecraft.world.level.block.WeatheringCopperSlabBlock;
import net.minecraft.world.level.block.WeatheringCopperStairBlock;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import snownee.kiwi.customization.block.BasicBlock;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.duck.KBlockProperties;
import snownee.kiwi.util.codec.CustomizationCodecs;

public class BlockCodecs {
	private static final Map<ResourceLocation, MapCodec<Block>> CODECS = Maps.newHashMap();

	public static final String BLOCK_PROPERTIES_KEY = "properties";
	private static final Codec<BlockBehaviour.Properties> BLOCK_PROPERTIES = new InjectedBlockPropertiesCodec(Codec.unit(BlockBehaviour.Properties::of));

	public static <B extends Block> RecordCodecBuilder<B, BlockBehaviour.Properties> propertiesCodec() {
		return BLOCK_PROPERTIES.fieldOf(BLOCK_PROPERTIES_KEY).forGetter(block -> block.properties);
	}

	public static <B extends Block> MapCodec<B> simpleCodec(Function<BlockBehaviour.Properties, B> function) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(propertiesCodec()).apply(instance, function));
	}

	public static final Function<BlockBehaviour.Properties, Block> SIMPLE_BLOCK_FACTORY = properties -> {
		KBlockSettings settings = ((KBlockProperties) properties).kiwi$getSettings();
		if (settings != null && settings.hasComponent(KBlockComponents.WATER_LOGGABLE.getOrCreate())) {
			return new BasicBlock(properties);
		} else {
			return new Block(properties);
		}
	};

	public static final MapCodec<Block> BLOCK = simpleCodec(SIMPLE_BLOCK_FACTORY);

	public static final MapCodec<StairBlock> STAIR = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockState.CODEC.optionalFieldOf("base_state", Blocks.AIR.defaultBlockState())
					.forGetter(block -> {throw new UnsupportedOperationException();}),
			propertiesCodec()
	).apply(instance, StairBlock::new));

	public static final MapCodec<DoorBlock> DOOR = RecordCodecBuilder.mapCodec(instance -> instance.group(
			propertiesCodec(),
			CustomizationCodecs.BLOCK_SET_TYPE.fieldOf("block_set_type").forGetter(DoorBlock::type)
	).apply(instance, DoorBlock::new));

	public static final MapCodec<TrapDoorBlock> TRAPDOOR = RecordCodecBuilder.mapCodec(instance -> instance.group(
			propertiesCodec(),
			CustomizationCodecs.BLOCK_SET_TYPE.fieldOf("block_set_type").forGetter(block -> block.type)
	).apply(instance, TrapDoorBlock::new));

	public static final MapCodec<FenceGateBlock> FENCE_GATE = woodTyped(FenceGateBlock::new);

	public static final MapCodec<SandBlock> SAND = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.optionalFieldOf("falling_dust_color", 14406560).forGetter($ -> 14406560),
			propertiesCodec()
	).apply(instance, SandBlock::new));

	public static final MapCodec<DropExperienceBlock> DROP_EXPERIENCE = RecordCodecBuilder.mapCodec(instance -> instance.group(
			propertiesCodec(),
			IntProvider.NON_NEGATIVE_CODEC.fieldOf("xp").forGetter(BlockCodecs::notImplemented)
	).apply(instance, DropExperienceBlock::new));

	public static final MapCodec<MushroomBlock> MUSHROOM = RecordCodecBuilder.mapCodec(instance -> instance.group(
			propertiesCodec(),
			ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(BlockCodecs::notImplemented)
	).apply(instance, MushroomBlock::new));

//	public static final MapCodec<SaplingBlock> SAPLING = RecordCodecBuilder.mapCodec(instance -> instance.group(
//			propertiesCodec(),
//			ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter($ -> BlockCodecs::notImplemented)
//	).apply(instance, SaplingBlock::new));

	public static final MapCodec<FlowerBlock> FLOWER = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("suspicious_stew_effect").forGetter(FlowerBlock::getSuspiciousEffect),
			ExtraCodecs.POSITIVE_INT.fieldOf("effect_duration").forGetter(FlowerBlock::getEffectDuration),
			propertiesCodec()
	).apply(instance, FlowerBlock::new));

	public static final MapCodec<FlowerPotBlock> FLOWER_POT = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BuiltInRegistries.BLOCK.byNameCodec().fieldOf("content").forGetter(FlowerPotBlock::getContent),
			propertiesCodec()
	).apply(instance, FlowerPotBlock::new));

	public static final MapCodec<WitherRoseBlock> WITHER_ROSE = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("effect").forGetter(FlowerBlock::getSuspiciousEffect),
			propertiesCodec()
	).apply(instance, WitherRoseBlock::new));

	public static final MapCodec<ButtonBlock> BUTTON = RecordCodecBuilder.mapCodec(instance -> instance.group(
			propertiesCodec(),
			CustomizationCodecs.BLOCK_SET_TYPE.fieldOf("block_set_type").forGetter(BlockCodecs::notImplemented),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("ticks_to_stay_pressed").forGetter(BlockCodecs::notImplemented),
			Codec.BOOL.optionalFieldOf("arrows_can_press").forGetter(BlockCodecs::notImplemented)
	).apply(
			instance, (
					(properties, blockSetType, ticksToStayPressed, arrowsCanPress) -> {
						return new ButtonBlock(
								properties,
								blockSetType,
								ticksToStayPressed.orElse(blockSetType.canOpenByHand() ? 30 : 20),
								arrowsCanPress.orElse(blockSetType.canOpenByHand()));
					})));

	public static final MapCodec<PressurePlateBlock> PRESSURE_PLATE = RecordCodecBuilder.mapCodec(instance -> instance.group(
			CustomizationCodecs.SENSITIVITY_CODEC.optionalFieldOf("sensitivity").forGetter(BlockCodecs::notImplemented),
			propertiesCodec(),
			CustomizationCodecs.BLOCK_SET_TYPE.fieldOf("block_set_type").forGetter(BlockCodecs::notImplemented)
	).apply(
			instance, (
					(sensitivity, properties, blockSetType) -> {
						return new PressurePlateBlock(
								sensitivity.orElse(blockSetType.canOpenByHand() ?
										PressurePlateBlock.Sensitivity.EVERYTHING :
										PressurePlateBlock.Sensitivity.MOBS), properties, blockSetType);
					})));

	public static final MapCodec<WeatheringCopperFullBlock> WEATHERING_COPPER_FULL = RecordCodecBuilder.mapCodec(instance -> instance.group(
			CustomizationCodecs.WEATHER_STATE.fieldOf("weather_state").forGetter(ChangeOverTimeBlock::getAge),
			propertiesCodec()
	).apply(instance, WeatheringCopperFullBlock::new));

	public static final MapCodec<WeatheringCopperSlabBlock> WEATHERING_COPPER_SLAB = RecordCodecBuilder.mapCodec(instance -> instance.group(
			CustomizationCodecs.WEATHER_STATE.fieldOf("weather_state").forGetter(ChangeOverTimeBlock::getAge),
			propertiesCodec()
	).apply(instance, WeatheringCopperSlabBlock::new));

	public static final MapCodec<WeatheringCopperStairBlock> WEATHERING_COPPER_STAIR = RecordCodecBuilder.mapCodec(instance -> instance.group(
			CustomizationCodecs.WEATHER_STATE.fieldOf("weather_state").forGetter(ChangeOverTimeBlock::getAge),
			BlockState.CODEC.fieldOf("base_state").forGetter($ -> {throw new UnsupportedOperationException();}),
			propertiesCodec()
	).apply(instance, WeatheringCopperStairBlock::new));

	public static final MapCodec<BedBlock> BED = RecordCodecBuilder.mapCodec(instance -> instance.group(
			DyeColor.CODEC.fieldOf("color").forGetter(BedBlock::getColor),
			propertiesCodec()
	).apply(instance, BedBlock::new));

	public static final MapCodec<BrushableBlock> BRUSHABLE = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BuiltInRegistries.BLOCK.byNameCodec().fieldOf("turns_into").forGetter(BrushableBlock::getTurnsInto),
			propertiesCodec(),
			BuiltInRegistries.SOUND_EVENT.byNameCodec()
					.optionalFieldOf("brush_sound", SoundEvents.BRUSH_SAND)
					.forGetter(BrushableBlock::getBrushSound),
			BuiltInRegistries.SOUND_EVENT.byNameCodec()
					.optionalFieldOf("brush_completed_sound", SoundEvents.BRUSH_SAND_COMPLETED)
					.forGetter(BrushableBlock::getBrushCompletedSound)
	).apply(instance, BrushableBlock::new));

	public static final MapCodec<WallSignBlock> WALL_SIGN = woodTyped(WallSignBlock::new);
	public static final MapCodec<StandingSignBlock> STANDING_SIGN = woodTyped(StandingSignBlock::new);
	public static final MapCodec<WallHangingSignBlock> WALL_HANGING_SIGN = woodTyped(WallHangingSignBlock::new);
	public static final MapCodec<CeilingHangingSignBlock> CEILING_HANGING_SIGN = woodTyped(CeilingHangingSignBlock::new);

	static {
		register("block", BLOCK);
		register("stair", STAIR);
		register("door", DOOR);
		register("trapdoor", TRAPDOOR);
		register("fence_gate", FENCE_GATE);
		register("colored_falling", SAND);
		register("drop_experience", DROP_EXPERIENCE);
		register("mushroom", MUSHROOM);
		register("flower", FLOWER);
		register("flower_pot", FLOWER_POT);
		register("wither_rose", WITHER_ROSE);
		register("button", BUTTON);
		register("pressure_plate", PRESSURE_PLATE);
		register("weathering_copper_full", WEATHERING_COPPER_FULL);
		register("weathering_copper_slab", WEATHERING_COPPER_SLAB);
		register("weathering_copper_stair", WEATHERING_COPPER_STAIR);
		register("bed", BED);
		register("brushable", BRUSHABLE);
		register("wall_sign", WALL_SIGN);
		register("standing_sign", STANDING_SIGN);
		register("wall_hanging_sign", WALL_HANGING_SIGN);
		register("ceiling_hanging_sign", CEILING_HANGING_SIGN);
	}

	public static void register(String key, MapCodec<? extends Block> codec) {
		register(new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, key), codec);
	}

	public static void register(ResourceLocation key, MapCodec<? extends Block> codec) {
		//noinspection unchecked
		CODECS.put(key, (MapCodec<Block>) codec);
	}

	public static MapCodec<Block> get(ResourceLocation key) {
		return Objects.requireNonNull(CODECS.get(key), key::toString);
	}

	public static <O, A> A notImplemented(O block) {
		throw new NotImplementedException();
	}

	public static <T extends Block> MapCodec<T> woodTyped(BiFunction<Block.Properties, WoodType, T> factory) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				propertiesCodec(),
				CustomizationCodecs.WOOD_TYPE.optionalFieldOf("wood_type", WoodType.OAK).forGetter($ -> WoodType.OAK)
		).apply(instance, factory));
	}
}
