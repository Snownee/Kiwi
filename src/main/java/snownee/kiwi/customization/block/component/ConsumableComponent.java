package snownee.kiwi.customization.block.component;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import snownee.kiwi.customization.block.KBlockUtils;
import snownee.kiwi.customization.block.behavior.BlockBehaviorRegistry;
import snownee.kiwi.customization.block.loader.KBlockComponents;
import snownee.kiwi.util.codec.CustomizationCodecs;

public record ConsumableComponent(
		IntegerProperty property,
		Optional<FoodProperties> food,
		Optional<ResourceKey<ResourceLocation>> stat) implements KBlockComponent, LayeredComponent {
	public static final MapCodec<ConsumableComponent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.intRange(0, 1).fieldOf("min").forGetter(ConsumableComponent::minValue),
			ExtraCodecs.POSITIVE_INT.fieldOf("max").forGetter(ConsumableComponent::maxValue),
			CustomizationCodecs.FOOD.optionalFieldOf("food").forGetter(ConsumableComponent::food),
			ResourceKey.codec(Registries.CUSTOM_STAT).optionalFieldOf("stat").forGetter(ConsumableComponent::stat)
	).apply(instance, ConsumableComponent::create));

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static ConsumableComponent create(
			int min,
			int max,
			Optional<FoodProperties> food,
			Optional<ResourceKey<ResourceLocation>> stat) {
		return new ConsumableComponent(KBlockUtils.internProperty(IntegerProperty.create("uses", min, max)), food, stat);
	}

	@Override
	public Type<?> type() {
		return KBlockComponents.CONSUMABLE.getOrCreate();
	}

	@Override
	public void injectProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(property);
	}

	@Override
	public BlockState registerDefaultState(BlockState state) {
		return state.setValue(property, getDefaultLayer());
	}

	public int minValue() {
		return property.min;
	}

	public int maxValue() {
		return property.max;
	}

	@Override
	public boolean hasAnalogOutputSignal() {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state) {
		return Math.min(state.getValue(property) - minValue() + 1, 15);
	}

	@Override
	public void addBehaviors(BlockBehaviorRegistry registry) {
		registry.addUseHandler((pState, pPlayer, pLevel, pHand, pHit) -> {
			int value = pState.getValue(property);
			if (value == 0) {
				return InteractionResult.PASS;
			}
			stat.map(ResourceKey::location).ifPresent(pPlayer::awardStat);
			BlockPos pos = pHit.getBlockPos();
			if (this.food.isPresent()) { //TODO block tag based drinking type
				FoodProperties food = this.food.get();
				if (!pPlayer.canEat(food.canAlwaysEat())) {
					return InteractionResult.FAIL;
				}
				Item item = pState.getBlock().asItem();
				pLevel.playSound(
						pPlayer,
						pPlayer.getX(),
						pPlayer.getY(),
						pPlayer.getZ(),
						item.getEatingSound(),
						SoundSource.NEUTRAL,
						1.0f,
						1.0f + (pLevel.random.nextFloat() - pLevel.random.nextFloat()) * 0.4f);
				if (!pLevel.isClientSide) {
					pPlayer.getFoodData().eat(food.nutrition(), food.saturation());
					for (var effect : food.effects()) {
						if (effect.effect() == null || !(pLevel.random.nextFloat() >= effect.probability())) {
							continue;
						}
						pPlayer.addEffect(effect.effect());
					}
				}
				pLevel.gameEvent(pPlayer, GameEvent.EAT, pos);
			}
			if (value == minValue()) {
				pLevel.removeBlock(pos, false);
			} else {
				pLevel.setBlockAndUpdate(pos, pState.setValue(property, value - 1));
			}
			return InteractionResult.sidedSuccess(pLevel.isClientSide);
		});
	}

	@Override
	public IntegerProperty getLayerProperty() {
		return property;
	}

	@Override
	public int getDefaultLayer() {
		return maxValue();
	}
}
