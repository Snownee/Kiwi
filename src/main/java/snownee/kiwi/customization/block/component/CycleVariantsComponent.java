package snownee.kiwi.customization.block.component;

import snownee.kiwi.customization.block.behavior.BlockBehaviorRegistry;
import snownee.kiwi.customization.block.loader.KBlockComponents;
import snownee.kiwi.customization.block.KBlockUtils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public record CycleVariantsComponent(IntegerProperty property, boolean rightClickToCycle) implements KBlockComponent, LayeredComponent {
	public static final Codec<CycleVariantsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(CycleVariantsComponent::maxValue),
			Codec.BOOL.optionalFieldOf("right_click_to_cycle", true).forGetter(CycleVariantsComponent::rightClickToCycle)
	).apply(instance, CycleVariantsComponent::create));

	public static CycleVariantsComponent create(int amount) {
		return create(amount, true);
	}

	public static CycleVariantsComponent create(int amount, boolean rightClickToCycle) {
		return new CycleVariantsComponent(KBlockUtils.internProperty(IntegerProperty.create("variant", 1, amount)), rightClickToCycle);
	}

	@Override
	public Type<?> type() {
		return KBlockComponents.CYCLE_VARIANTS.getOrCreate();
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
	public IntegerProperty getLayerProperty() {
		return property;
	}

	@Override
	public int getDefaultLayer() {
		return minValue();
	}

	@Override
	public void addBehaviors(BlockBehaviorRegistry registry) {
		if (rightClickToCycle) {
			registry.addUseHandler((pState, pPlayer, pLevel, pHand, pHit) -> {
				BlockState newState = pState.cycle(property);
				pLevel.setBlock(pHit.getBlockPos(), newState, 3);
				return InteractionResult.sidedSuccess(pLevel.isClientSide);
			});
		}
	}
}
