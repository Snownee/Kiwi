package snownee.kiwi.customization.block.loader;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import snownee.kiwi.customization.block.BlockFundamentals;
import snownee.kiwi.customization.block.GlassType;
import snownee.kiwi.customization.block.behavior.CanSurviveHandler;
import snownee.kiwi.customization.block.behavior.CanSurviveHandlerCodec;
import snownee.kiwi.customization.block.component.KBlockComponent;
import snownee.kiwi.util.codec.CustomizationCodecs;

public record BlockDefinitionProperties(
		List<Either<KBlockComponent, String>> components,
		Optional<KMaterial> material,
		Optional<GlassType> glassType,
		Optional<List<Identifier>> colorProvider,
		Optional<Identifier> shape,
		Optional<Identifier> collisionShape,
		Optional<Identifier> interactionShape,
		Optional<CanSurviveHandler> canSurviveHandler,
		PartialVanillaProperties vanillaProperties) {
	public static MapCodec<BlockDefinitionProperties> mapCodec(BlockFundamentals.CodecCreationContext context) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.either(KBlockComponent.CODEC, Codec.STRING)
						.listOf()
						.optionalFieldOf("components", List.of())
						.forGetter(BlockDefinitionProperties::components),
				context.materialCodec().forGetter(BlockDefinitionProperties::material),
				context.glassTypeCodec().forGetter(BlockDefinitionProperties::glassType),
				ExtraCodecs.compactListCodec(Identifier.CODEC)
						.optionalFieldOf("color_provider")
						.forGetter(BlockDefinitionProperties::colorProvider),
				Identifier.CODEC.optionalFieldOf("shape").forGetter(BlockDefinitionProperties::shape),
				Identifier.CODEC.optionalFieldOf("collision_shape").forGetter(BlockDefinitionProperties::collisionShape),
				Identifier.CODEC.optionalFieldOf("interaction_shape").forGetter(BlockDefinitionProperties::interactionShape),
				new CanSurviveHandlerCodec().optionalFieldOf("can_survive_handler").forGetter(BlockDefinitionProperties::canSurviveHandler),
				PartialVanillaProperties.MAP_CODEC.forGetter(BlockDefinitionProperties::vanillaProperties)
		).apply(instance, BlockDefinitionProperties::new));
	}

	public static MapCodec<Optional<BlockDefinitionProperties>> mapCodecField(BlockFundamentals.CodecCreationContext context) {
		return mapCodec(context).codec().optionalFieldOf(BlockCodecs.BLOCK_PROPERTIES_KEY);
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
			Optional<ResourceKey<Block>> copy,
			Optional<Boolean> noCollision,
			Optional<Boolean> isRandomlyTicking,
			Optional<Integer> lightEmission,
			Optional<Boolean> dynamicShape,
			Optional<Boolean> noOcclusion,
			Optional<Boolean> legacySolid,
			Optional<PushReaction> pushReaction,
			Optional<BlockBehaviour.OffsetType> offsetType,
			Optional<Boolean> replaceable,
			Optional<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> isValidSpawn,
			Optional<BlockBehaviour.StatePredicate> isRedstoneConductor,
			Optional<BlockBehaviour.StatePredicate> isSuffocating,
			Optional<BlockBehaviour.StatePredicate> isViewBlocking,
			Optional<BlockBehaviour.PostProcess> postProcess,
			Optional<BlockBehaviour.StatePredicate> emissiveRendering) {
		public static final MapCodec<PartialVanillaProperties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ResourceKey.codec(Registries.BLOCK).optionalFieldOf("copy").forGetter(PartialVanillaProperties::copy),
				Codec.BOOL.optionalFieldOf("no_collision").forGetter(PartialVanillaProperties::noCollision),
				Codec.BOOL.optionalFieldOf("is_randomly_ticking").forGetter(PartialVanillaProperties::isRandomlyTicking),
				Codec.INT.optionalFieldOf("light_emission").forGetter(PartialVanillaProperties::lightEmission),
				Codec.BOOL.optionalFieldOf("dynamic_shape").forGetter(PartialVanillaProperties::dynamicShape),
				Codec.BOOL.optionalFieldOf("no_occlusion").forGetter(PartialVanillaProperties::noOcclusion),
				Codec.BOOL.optionalFieldOf("legacy_solid").forGetter(PartialVanillaProperties::legacySolid),
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
				CustomizationCodecs.POST_PROCESS.optionalFieldOf("post_process").forGetter(PartialVanillaProperties::postProcess),
				CustomizationCodecs.STATE_PREDICATE.optionalFieldOf("emissive_rendering")
						.forGetter(PartialVanillaProperties::emissiveRendering)
		).apply(instance, PartialVanillaProperties::new));

		public PartialVanillaProperties merge(PartialVanillaProperties templateProps) {
			return new PartialVanillaProperties(
					or(this.copy, templateProps.copy),
					or(this.noCollision, templateProps.noCollision),
					or(this.isRandomlyTicking, templateProps.isRandomlyTicking),
					or(this.lightEmission, templateProps.lightEmission),
					or(this.dynamicShape, templateProps.dynamicShape),
					or(this.noOcclusion, templateProps.noOcclusion),
					or(this.legacySolid, templateProps.legacySolid),
					or(this.pushReaction, templateProps.pushReaction),
					or(this.offsetType, templateProps.offsetType),
					or(this.replaceable, templateProps.replaceable),
					or(this.isValidSpawn, templateProps.isValidSpawn),
					or(this.isRedstoneConductor, templateProps.isRedstoneConductor),
					or(this.isSuffocating, templateProps.isSuffocating),
					or(this.isViewBlocking, templateProps.isViewBlocking),
					or(this.postProcess, templateProps.postProcess),
					or(this.emissiveRendering, templateProps.emissiveRendering));
		}
	}
}
