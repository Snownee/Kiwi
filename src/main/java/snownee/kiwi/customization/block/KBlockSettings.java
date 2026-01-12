package snownee.kiwi.customization.block;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.customization.block.behavior.CanSurviveHandler;
import snownee.kiwi.customization.block.component.DirectionalComponent;
import snownee.kiwi.customization.block.component.HorizontalComponent;
import snownee.kiwi.customization.block.component.KBlockComponent;
import snownee.kiwi.customization.block.component.WaterLoggableComponent;
import snownee.kiwi.customization.duck.KBlockProperties;
import snownee.kiwi.customization.placement.PlaceChoices;
import snownee.kiwi.customization.shape.BlockShapeType;
import snownee.kiwi.customization.shape.ConfiguringShape;
import snownee.kiwi.customization.shape.ShapeGenerator;

public class KBlockSettings {
	public final boolean customPlacement;
	@Nullable
	public final GlassType glassType;
	@Nullable
	public final CanSurviveHandler canSurviveHandler;
	@Nullable
	public final ToIntFunction<BlockState> analogOutputSignal;
	public final Map<KBlockComponent.Type<?>, KBlockComponent> components;
	@Nullable
	private ShapeGenerator[] shapes;
	@Nullable
	public PlaceChoices placeChoices;

	private KBlockSettings(Builder builder) {
		this.customPlacement = builder.customPlacement;
		this.glassType = builder.glassType;
		this.canSurviveHandler = builder.canSurviveHandler;
		this.analogOutputSignal = builder.getAnalogOutputSignal();
		this.components = Map.copyOf(builder.components);
		for (BlockShapeType type : BlockShapeType.VALUES) {
			setShape(type, builder.getShape(type));
		}
//		if (Platform.isPhysicalClient() && XKDecoClientConfig.exportBlocksMore) {
//			if (builder.shapeId != null || builder.collisionShapeId != null || builder.interactionShapeId != null) {
//				ExportBlocksCommand.putMoreInfo(this, new MoreInfo(builder.shapeId, builder.collisionShapeId, builder.interactionShapeId));
//			}
//		}
	}

	public static KBlockSettings defaulted(Block block) {
		return new KBlockSettings(builder(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow()));
	}

	public static KBlockSettings.Builder builder(ResourceKey<Block> key) {
		return new Builder(BlockBehaviour.Properties.of().setId(key));
	}

	public static KBlockSettings.Builder copyProperties(ResourceKey<Block> key, Block block) {
		return new Builder(BlockBehaviour.Properties.ofFullCopy(block).setId(key));
	}

	public static KBlockSettings.Builder copyProperties(ResourceKey<Block> key, Block block, MapColor mapColor) {
		return new Builder(BlockBehaviour.Properties.ofFullCopy(block).setId(key).mapColor(mapColor));
	}

	@Nullable
	public static KBlockSettings of(Object block) {
		return ((KBlockProperties) ((BlockBehaviour) block).properties()).kiwi$getSettings();
	}

	public static VoxelShape getGlassFaceShape(BlockState blockState, Direction direction) {
		KBlockSettings settings = of(blockState.getBlock());
		if (settings == null) {
			VoxelShape shape = blockState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
			return Block.isShapeFullBlock(shape) ? Shapes.block() : Shapes.empty();
		}
		if (settings.glassType == null) {
			return Shapes.empty();
		}
		VoxelShape shape = blockState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
		if (shape.isEmpty()) {
			return Shapes.empty();
		}
		return shape.getFaceShape(direction);
	}

	public boolean hasComponent(KBlockComponent.Type<?> type) {
		return components.containsKey(type);
	}

	@SuppressWarnings("unchecked")
	public <T extends KBlockComponent> T getComponent(KBlockComponent.Type<T> type) {
		return (T) components.get(type);
	}

	public void injectProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
		for (KBlockComponent component : components.values()) {
			component.injectProperties(block, builder);
		}
	}

	public BlockState registerDefaultState(BlockState state) {
		for (KBlockComponent component : components.values()) {
			state = component.registerDefaultState(state);
		}
		return state;
	}

	@Nullable
	public BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext context) {
		for (KBlockComponent component : components.values()) {
			blockState = component.getStateForPlacement(this, blockState, context);
			if (blockState == null || !blockState.is(blockState.getBlock())) {
				return blockState;
			}
		}
		if (placeChoices != null && (!placeChoices.skippable() || !context.isSecondaryUseActive())) {
			blockState = placeChoices.getStateForPlacement(context.getLevel(), context.getClickedPos(), blockState);
		}
		return blockState;
	}

	public BlockState updateShape(
			BlockState pState,
			Direction pDirection,
			BlockState pNeighborState,
			LevelReader pLevel,
			ScheduledTickAccess scheduledTickAccess,
			BlockPos pPos,
			BlockPos pNeighborPos) {
		for (KBlockComponent component : components.values()) {
			pState = component.updateShape(pState, pDirection, pNeighborState, pLevel, scheduledTickAccess, pPos, pNeighborPos);
		}
		return pState;
	}

	public BlockState rotate(BlockState pState, Rotation pRotation) {
		for (KBlockComponent component : components.values()) {
			pState = component.rotate(pState, pRotation);
		}
		return pState;
	}

	public BlockState mirror(BlockState pState, Mirror pMirror) {
		for (KBlockComponent component : components.values()) {
			pState = component.mirror(pState, pMirror);
		}
		return pState;
	}

	public boolean useShapeForLightOcclusion(BlockState pState) {
		for (KBlockComponent component : components.values()) {
			if (component.useShapeForLightOcclusion(pState)) {
				return true;
			}
		}
		return false;
	}

	@Nullable
	public Boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		for (KBlockComponent component : components.values()) {
			Boolean result = component.canBeReplaced(state, context);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Nullable
	public ConfiguringShape removeIfPossible(BlockShapeType shapeType) {
		if (getShape(shapeType) instanceof ConfiguringShape shape) {
			setShape(shapeType, null);
			return shape;
		}
		return null;
	}

	@Nullable
	public ShapeGenerator getShape(BlockShapeType shapeType) {
		return shapes != null ? shapes[shapeType.ordinal()] : null;
	}

	private void setShape(BlockShapeType shapeType, @Nullable ShapeGenerator shape) {
		if (shape != null) {
			if (shapes == null) {
				shapes = new ShapeGenerator[BlockShapeType.VALUES.size()];
			}
			shapes[shapeType.ordinal()] = shape;
		} else if (shapes != null) {
			shapes[shapeType.ordinal()] = null;
		}
	}

	public static class Builder {
		private final BlockBehaviour.Properties properties;
		private boolean customPlacement;
		@Nullable
		private GlassType glassType;
		private final ShapeGenerator[] shapes = new ShapeGenerator[BlockShapeType.VALUES.size()];
		@Nullable
		private CanSurviveHandler canSurviveHandler;
		private final Map<KBlockComponent.Type<?>, KBlockComponent> components = Maps.newLinkedHashMap();
		@Nullable
		private ToIntFunction<BlockState> analogOutputSignal;

		private Builder(BlockBehaviour.Properties properties) {
			this.properties = properties;
		}

		public BlockBehaviour.Properties get() {
			KBlockSettings settings = new KBlockSettings(this);
			((KBlockProperties) properties).kiwi$setSettings(settings);
			return properties;
		}

		@CanIgnoreReturnValue
		public Builder configure(Consumer<BlockBehaviour.Properties> configurator) {
			configurator.accept(properties);
			return this;
		}

		public Builder noOcclusion() {
			properties.noOcclusion();
			return this;
		}

		public Builder noCollision() {
			properties.noCollission();
			return this;
		}

		public Builder customPlacement() {
			this.customPlacement = true;
			return this;
		}

		@CanIgnoreReturnValue
		public Builder glassType(GlassType glassType) {
			this.glassType = glassType;
			return this;
		}

		public Builder shape(BlockShapeType type, ShapeGenerator shape) {
			shapes[type.ordinal()] = shape;
			return this;
		}

		@Nullable
		private ShapeGenerator getShape(BlockShapeType type) {
			return shapes[type.ordinal()];
		}

		@CanIgnoreReturnValue
		public Builder canSurviveHandler(CanSurviveHandler canSurviveHandler) {
			this.canSurviveHandler = canSurviveHandler;
			return this;
		}

		public Builder component(KBlockComponent component) {
			KBlockComponent before = this.components.put(component.type(), component);
			Preconditions.checkState(before == null, "Component %s is already present", component.type());
			return this;
		}

		public Builder waterLoggable() {
			return component(WaterLoggableComponent.getInstance());
		}

		public Builder horizontal() {
			return component(HorizontalComponent.getInstance(false));
		}

		public Builder directional() {
			return component(DirectionalComponent.getInstance(false));
		}

		public boolean hasComponent(KBlockComponent.Type<?> type) {
			return components.containsKey(type);
		}

		@CanIgnoreReturnValue
		public Builder removeComponent(KBlockComponent.Type<?> type) {
			components.remove(type);
			return this;
		}

		public @Nullable ToIntFunction<BlockState> getAnalogOutputSignal() {
			if (analogOutputSignal != null) {
				return analogOutputSignal;
			}
			for (KBlockComponent component : components.values()) {
				if (component.hasAnalogOutputSignal()) {
					return component::getAnalogOutputSignal;
				}
			}
			return null;
		}
	}

	@Deprecated
	public record MoreInfo(ResourceLocation shape, ResourceLocation collisionShape, ResourceLocation interactionShape) {
	}
}