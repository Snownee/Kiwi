package snownee.kiwi.customization.block.component;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import snownee.kiwi.customization.block.KBlockUtils;
import snownee.kiwi.customization.block.behavior.BlockBehaviorRegistry;
import snownee.kiwi.customization.block.loader.KBlockComponents;

public record ConsumableComponent(
		IntegerProperty property,
		DataComponentMap components,
		Optional<ResourceKey<Identifier>> stat) implements KBlockComponent, LayeredComponent, DataComponentHolder {
	public static final MapCodec<ConsumableComponent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.intRange(0, 1).fieldOf("min").forGetter(ConsumableComponent::minValue),
			ExtraCodecs.POSITIVE_INT.fieldOf("max").forGetter(ConsumableComponent::maxValue),
			DataComponentMap.CODEC.optionalFieldOf("components", DataComponentMap.EMPTY).forGetter(ConsumableComponent::components),
			ResourceKey.codec(Registries.CUSTOM_STAT).optionalFieldOf("stat").forGetter(ConsumableComponent::stat)
	).apply(instance, ConsumableComponent::create));

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static ConsumableComponent create(
			int min,
			int max,
			DataComponentMap components,
			Optional<ResourceKey<Identifier>> stat) {
		return new ConsumableComponent(KBlockUtils.internProperty(IntegerProperty.create("uses", min, max)), components, stat);
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
			stat.map(ResourceKey::identifier).ifPresent(pPlayer::awardStat);
			ItemStack itemStack = pState.getBlock().asItem().getDefaultInstance();
			Consumable consumable = get(DataComponents.CONSUMABLE);
			if (consumable != null && consumable.canConsume(pPlayer, itemStack)) {
				consumable.onConsume(pLevel, pPlayer, itemStack);
			}
			BlockPos pos = pHit.getBlockPos();
			if (value == minValue()) {
				pLevel.removeBlock(pos, false);
			} else {
				pLevel.setBlockAndUpdate(pos, pState.setValue(property, value - 1));
			}
			return InteractionResult.SUCCESS_SERVER;
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

	@Override
	public DataComponentMap getComponents() {
		return components;
	}
}
