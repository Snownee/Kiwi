package snownee.kiwi.customization.block.loader;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import snownee.kiwi.RenderLayerEnum;
import snownee.kiwi.customization.block.GlassType;
import snownee.kiwi.customization.block.behavior.CanSurviveHandler;
import snownee.kiwi.customization.block.behavior.CanSurviveHandlerCodec;
import snownee.kiwi.customization.block.component.KBlockComponent;
import snownee.kiwi.util.codec.CustomizationCodecs;

public record BlockDefinitionProperties(
		List<Either<KBlockComponent, String>> components,
		Optional<KMaterial> material,
		Optional<GlassType> glassType,
		Optional<RenderLayerEnum> renderType,
		Optional<ResourceLocation> colorProvider,
		Optional<ResourceLocation> shape,
		Optional<ResourceLocation> collisionShape,
		Optional<ResourceLocation> interactionShape,
		Optional<CanSurviveHandler> canSurviveHandler,
		PartialVanillaProperties vanillaProperties) {
	public static MapCodec<BlockDefinitionProperties> mapCodec(MapCodec<Optional<KMaterial>> materialCodec) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.either(KBlockComponent.DIRECT_CODEC, Codec.STRING)
						.listOf()
						.optionalFieldOf("components", List.of())
						.forGetter(BlockDefinitionProperties::components),
				materialCodec.forGetter(BlockDefinitionProperties::material),
				CustomizationCodecs.GLASS_TYPE_CODEC.optionalFieldOf("glass_type").forGetter(BlockDefinitionProperties::glassType),
				CustomizationCodecs.RENDER_TYPE.optionalFieldOf("render_type").forGetter(BlockDefinitionProperties::renderType),
				ResourceLocation.CODEC.optionalFieldOf("color_provider").forGetter(BlockDefinitionProperties::colorProvider),
				ResourceLocation.CODEC.optionalFieldOf("shape").forGetter(BlockDefinitionProperties::shape),
				ResourceLocation.CODEC.optionalFieldOf("collision_shape").forGetter(BlockDefinitionProperties::collisionShape),
				ResourceLocation.CODEC.optionalFieldOf("interaction_shape").forGetter(BlockDefinitionProperties::interactionShape),
				new CanSurviveHandlerCodec().optionalFieldOf("can_survive_handler").forGetter(BlockDefinitionProperties::canSurviveHandler),
				PartialVanillaProperties.MAP_CODEC.forGetter(BlockDefinitionProperties::vanillaProperties)
		).apply(instance, BlockDefinitionProperties::new));
	}

	public static MapCodec<Optional<BlockDefinitionProperties>> mapCodecField(MapCodec<Optional<KMaterial>> materialCodec) {
		return mapCodec(materialCodec).codec().optionalFieldOf(BlockCodecs.BLOCK_PROPERTIES_KEY);
	}

	public BlockDefinitionProperties merge(BlockDefinitionProperties templateProps) {
		List<Either<KBlockComponent, String>> components;
		if (this.components.isEmpty()) {
			components = templateProps.components;
		} else if (templateProps.components.isEmpty()) {
			components = this.components;
		} else {
			components = Lists.newArrayListWithExpectedSize(this.components.size() + templateProps.components.size());
			components.addAll(this.components);
			components.addAll(templateProps.components);
		}
		return new BlockDefinitionProperties(
				components,
				or(this.material, templateProps.material),
				or(this.glassType, templateProps.glassType),
				or(this.renderType, templateProps.renderType),
				or(this.colorProvider, templateProps.colorProvider),
				or(this.shape, templateProps.shape),
				or(this.collisionShape, templateProps.collisionShape),
				or(this.interactionShape, templateProps.interactionShape),
				or(this.canSurviveHandler, templateProps.canSurviveHandler),
				vanillaProperties.merge(templateProps.vanillaProperties));
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static <T> Optional<T> or(Optional<T> a, Optional<T> b) {
		return a.isPresent() ? a : b;
	}

	public record PartialVanillaProperties(
			Optional<Boolean> noCollision,
			Optional<Boolean> isRandomlyTicking,
			Optional<Integer> lightEmission,
			Optional<Boolean> dynamicShape,
			Optional<Boolean> noOcclusion,
			Optional<PushReaction> pushReaction,
			Optional<BlockBehaviour.OffsetType> offsetType,
			Optional<Boolean> replaceable,
			Optional<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> isValidSpawn,
			Optional<BlockBehaviour.StatePredicate> isRedstoneConductor,
			Optional<BlockBehaviour.StatePredicate> isSuffocating,
			Optional<BlockBehaviour.StatePredicate> isViewBlocking,
			Optional<BlockBehaviour.StatePredicate> hasPostProcess,
			Optional<BlockBehaviour.StatePredicate> emissiveRendering) {
		public static final MapCodec<PartialVanillaProperties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("no_collision").forGetter(PartialVanillaProperties::noCollision),
				Codec.BOOL.optionalFieldOf("is_randomly_ticking").forGetter(PartialVanillaProperties::isRandomlyTicking),
				Codec.INT.optionalFieldOf("light_emission").forGetter(PartialVanillaProperties::lightEmission),
				Codec.BOOL.optionalFieldOf("dynamic_shape").forGetter(PartialVanillaProperties::dynamicShape),
				Codec.BOOL.optionalFieldOf("no_occlusion").forGetter(PartialVanillaProperties::noOcclusion),
				CustomizationCodecs.PUSH_REACTION.optionalFieldOf("push_reaction").forGetter(PartialVanillaProperties::pushReaction),
				CustomizationCodecs.OFFSET_TYPE.optionalFieldOf("offset_function")
						.forGetter(PartialVanillaProperties::offsetType),
				Codec.BOOL.optionalFieldOf("replaceable").forGetter(PartialVanillaProperties::replaceable),
				CustomizationCodecs.<EntityType<?>>stateArgumentPredicate().optionalFieldOf("is_valid_spawn")
						.forGetter(PartialVanillaProperties::isValidSpawn),
				CustomizationCodecs.STATE_PREDICATE.optionalFieldOf("is_redstone_conductor")
						.forGetter(PartialVanillaProperties::isRedstoneConductor),
				CustomizationCodecs.STATE_PREDICATE.optionalFieldOf("is_suffocating").forGetter(PartialVanillaProperties::isSuffocating),
				CustomizationCodecs.STATE_PREDICATE.optionalFieldOf("is_view_blocking").forGetter(PartialVanillaProperties::isViewBlocking),
				CustomizationCodecs.STATE_PREDICATE.optionalFieldOf("has_post_process").forGetter(PartialVanillaProperties::hasPostProcess),
				CustomizationCodecs.STATE_PREDICATE.optionalFieldOf("emissive_rendering")
						.forGetter(PartialVanillaProperties::emissiveRendering)
		).apply(instance, PartialVanillaProperties::new));

		public PartialVanillaProperties merge(PartialVanillaProperties templateProps) {
			return new PartialVanillaProperties(
					or(this.noCollision, templateProps.noCollision),
					or(this.isRandomlyTicking, templateProps.isRandomlyTicking),
					or(this.lightEmission, templateProps.lightEmission),
					or(this.dynamicShape, templateProps.dynamicShape),
					or(this.noOcclusion, templateProps.noOcclusion),
					or(this.pushReaction, templateProps.pushReaction),
					or(this.offsetType, templateProps.offsetType),
					or(this.replaceable, templateProps.replaceable),
					or(this.isValidSpawn, templateProps.isValidSpawn),
					or(this.isRedstoneConductor, templateProps.isRedstoneConductor),
					or(this.isSuffocating, templateProps.isSuffocating),
					or(this.isViewBlocking, templateProps.isViewBlocking),
					or(this.hasPostProcess, templateProps.hasPostProcess),
					or(this.emissiveRendering, templateProps.emissiveRendering));
		}
	}
}
