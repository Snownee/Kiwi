package snownee.kiwi.customization.block.loader;

import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.CustomizationRegistries;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.block.behavior.BlockBehaviorRegistry;
import snownee.kiwi.customization.block.component.KBlockComponent;
import snownee.kiwi.customization.shape.BlockShapeType;
import snownee.kiwi.customization.shape.ChoicesShape;
import snownee.kiwi.customization.shape.ConfiguringShape;
import snownee.kiwi.customization.shape.DirectionalShape;
import snownee.kiwi.customization.shape.HorizontalShape;
import snownee.kiwi.customization.shape.MouldingShape;
import snownee.kiwi.customization.shape.ShapeGenerator;
import snownee.kiwi.customization.shape.ShapeStorage;
import snownee.kiwi.util.codec.CustomizationCodecs;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.VanillaActions;
import snownee.kiwi.util.VoxelUtil;

public record KBlockDefinition(ConfiguredBlockTemplate template, BlockDefinitionProperties properties) {
	public KBlockDefinition(ConfiguredBlockTemplate template, BlockDefinitionProperties properties) {
		this.template = template;
		this.properties = template.template().properties().map(properties::merge).orElse(properties);
	}

	public static Codec<KBlockDefinition> codec(
			Map<ResourceLocation, KBlockTemplate> templates,
			MapCodec<Optional<KMaterial>> materialCodec) {
		KBlockTemplate defaultTemplate = templates.get(new ResourceLocation("block"));
		Preconditions.checkNotNull(defaultTemplate);
		ConfiguredBlockTemplate defaultConfiguredTemplate = new ConfiguredBlockTemplate(defaultTemplate);
		return RecordCodecBuilder.create(instance -> instance.group(
				CustomizationCodecs.strictOptionalField(
								ConfiguredBlockTemplate.codec(templates),
								"template",
								defaultConfiguredTemplate)
						.forGetter(KBlockDefinition::template),
				BlockDefinitionProperties.mapCodec(materialCodec).forGetter(KBlockDefinition::properties)
		).apply(instance, KBlockDefinition::new));
	}

	public KBlockSettings.Builder createSettings(ResourceLocation id, ShapeStorage shapes) {
		KBlockSettings.Builder builder = KBlockSettings.builder();
		properties.glassType().ifPresent(builder::glassType);
		BlockDefinitionProperties.PartialVanillaProperties vanilla = properties.vanillaProperties();
		builder.configure($ -> {
			vanilla.lightEmission().ifPresent(i -> $.lightLevel($$ -> i));
			vanilla.pushReaction().ifPresent($::pushReaction);
			vanilla.emissiveRendering().ifPresent($::emissiveRendering);
			vanilla.hasPostProcess().ifPresent($::hasPostProcess);
			vanilla.isRedstoneConductor().ifPresent($::isRedstoneConductor);
			vanilla.isSuffocating().ifPresent($::isSuffocating);
			vanilla.isViewBlocking().ifPresent($::isViewBlocking);
			vanilla.isValidSpawn().ifPresent($::isValidSpawn);
			vanilla.offsetType().ifPresent($::offsetType);
			if (vanilla.noCollision().orElse(false)) {
				$.noCollission();
			}
			if (vanilla.noOcclusion().orElse(false)) {
				$.noOcclusion();
			}
			if (vanilla.isRandomlyTicking().orElse(false)) {
				$.randomTicks();
			}
			if (vanilla.dynamicShape().orElse(false)) {
				$.dynamicShape();
			}
			if (vanilla.replaceable().orElse(false)) {
				$.replaceable();
			}
		});
		properties.material().ifPresent(mat -> {
			builder.configure($ -> {
				$.strength(mat.destroyTime(), mat.explosionResistance());
				$.sound(mat.soundType());
				$.instrument(mat.instrument());
				$.mapColor(mat.defaultMapColor());
				if (mat.ignitedByLava()) {
					$.ignitedByLava();
				}
				if (mat.requiresCorrectToolForDrops()) {
					$.requiresCorrectToolForDrops();
				}
			});
		});
		if (properties.material().isEmpty()) {
			builder.configure($ -> $.strength(2, 3));
		}
		properties.canSurviveHandler().ifPresent(builder::canSurviveHandler);
		for (Either<KBlockComponent, String> component : properties.components()) {
			if (component.left().isPresent()) {
				builder.component(component.left().get());
			} else {
				String s = component.right().orElseThrow();
				boolean remove = s.startsWith("-");
				if (remove) {
					s = s.substring(1);
				}
				KBlockComponent.Type<?> type = CustomizationRegistries.BLOCK_COMPONENT.get(new ResourceLocation(s));
				Preconditions.checkNotNull(type, "Unknown component type %s", s);
				if (remove) {
					builder.removeComponent(type);
				} else {
					builder.component(KBlockComponents.getSimpleInstance(type));
				}
			}
		}
		if (!Platform.isDataGen()) {
			deriveAndSetShape(shapes, builder, BlockShapeType.MAIN, properties.shape());
			deriveAndSetShape(shapes, builder, BlockShapeType.COLLISION, properties.collisionShape());
			deriveAndSetShape(shapes, builder, BlockShapeType.INTERACTION, properties.interactionShape());
		}
		return builder;
	}

	public Block createBlock(ResourceLocation id, ShapeStorage shapes) {
		KBlockSettings.Builder builder = createSettings(id, shapes);
		Block block = template.template().createBlock(id, builder.get(), template.json());
		setConfiguringShape(block);
		properties.material().ifPresent(mat -> {
			VanillaActions.setFireInfo(block, mat.igniteOdds(), mat.burnOdds());
		});
		KBlockSettings settings = KBlockSettings.of(block);
		BlockBehaviorRegistry behaviorRegistry = BlockBehaviorRegistry.getInstance();
		for (KBlockComponent component : settings.components.values()) {
			behaviorRegistry.setContext(block);
			component.addBehaviors(behaviorRegistry);
		}
		behaviorRegistry.setContext(null);
		return block;
	}

	public static void setConfiguringShape(Block block) {
		KBlockSettings settings = KBlockSettings.of(block);
		if (settings == null) {
			return;
		}
		for (BlockShapeType shapeType : BlockShapeType.VALUES) {
			ConfiguringShape shape = settings.removeIfPossible(shapeType);
			if (shape != null) {
				shape.configure(block, shapeType);
			}
		}
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private void deriveAndSetShape(
			ShapeStorage shapes,
			KBlockSettings.Builder builder,
			BlockShapeType type,
			Optional<ResourceLocation> shapeId) {
		if (shapeId.isEmpty()) {
			return;
		}
		ShapeGenerator shape = shapes.get(shapeId.get());
		if (shape == null) {
			Kiwi.LOGGER.warn("Shape {} is not registered", shapeId.get());
			return;
		}
		if (shape.getClass() != ShapeGenerator.Unit.class) {
			builder.shape(type, shape);
			return;
		}
		if (builder.hasComponent(KBlockComponents.HORIZONTAL.getOrCreate())) {
			shape = shapes.transform(shape, KBlockComponents.HORIZONTAL.getOrCreate(), HorizontalShape::create);
		} else if (builder.hasComponent(KBlockComponents.DIRECTIONAL.getOrCreate())) {
			shape = shapes.transform(shape, KBlockComponents.DIRECTIONAL.getOrCreate(), $ -> DirectionalShape.create($, "facing"));
		} else if (builder.hasComponent(KBlockComponents.MOULDING.getOrCreate())) {
			shape = shapes.transform(shape, KBlockComponents.MOULDING.getOrCreate(), MouldingShape::create);
		} else if (builder.hasComponent(KBlockComponents.FRONT_AND_TOP.getOrCreate())) {
			shape = shapes.transform(shape, KBlockComponents.FRONT_AND_TOP.getOrCreate(), $ -> DirectionalShape.create($, "orientation"));
		} else if (builder.hasComponent(KBlockComponents.HORIZONTAL_AXIS.getOrCreate())) {
			shape = shapes.transform(shape, KBlockComponents.HORIZONTAL_AXIS.getOrCreate(), $ -> ChoicesShape.chooseOneProperty(
					BlockStateProperties.HORIZONTAL_AXIS,
					Map.of(
							Direction.Axis.X,
							$,
							Direction.Axis.Z,
							ShapeGenerator.unit(VoxelUtil.rotateHorizontal(ShapeGenerator.Unit.unboxOrThrow($), Direction.EAST)))));
		}
		builder.shape(type, shape);
	}
}
