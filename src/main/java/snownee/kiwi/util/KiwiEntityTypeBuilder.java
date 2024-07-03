package snownee.kiwi.util;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class KiwiEntityTypeBuilder<T extends Entity> {
	private final Class<?> type;
	private EntityType.EntityFactory<T> factory;
	private MobCategory category = MobCategory.MISC;
	private ImmutableSet<Block> immuneTo = ImmutableSet.of();
	private boolean serialize = true;
	private boolean summon = true;
	private boolean fireImmune;
	private boolean canSpawnFarFromPlayer;
	private int clientTrackingRange = 5;
	private int updateInterval = 3;
	private EntityDimensions dimensions = EntityDimensions.scalable(0.6F, 1.8F);
	private float spawnDimensionsScale = 1.0F;
	private EntityAttachments.Builder attachments = EntityAttachments.builder();
	private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
	private Boolean forceTrackedVelocityUpdates;
	@Nullable
	private Supplier<AttributeSupplier.Builder> defaultAttributeBuilder;
	private SpawnPlacementType restrictionLocation;
	private Heightmap.Types restrictionHeightmap;
	private SpawnPlacements.SpawnPredicate<T> spawnPredicate;

	private KiwiEntityTypeBuilder(Class<?> type) {
		this.type = type;
	}

	public static <T extends Entity> KiwiEntityTypeBuilder<T> create() {
		return new KiwiEntityTypeBuilder<>(Entity.class);
	}

	public static <T extends LivingEntity> KiwiEntityTypeBuilder<T> createLiving() {
		return new KiwiEntityTypeBuilder<>(LivingEntity.class);
	}

	public static <T extends Entity> KiwiEntityTypeBuilder<T> createMob() {
		return new KiwiEntityTypeBuilder<>(Mob.class);
	}

	public KiwiEntityTypeBuilder<T> spawnGroup(MobCategory group) {
		Objects.requireNonNull(group, "Spawn group cannot be null");
		this.category = group;
		return this;
	}

	public <N extends T> KiwiEntityTypeBuilder<N> entityFactory(EntityType.EntityFactory<N> factory) {
		Objects.requireNonNull(factory, "Entity Factory cannot be null");
		this.factory = (EntityType.EntityFactory<T>) factory;
		return (KiwiEntityTypeBuilder<N>) this;
	}

	public KiwiEntityTypeBuilder<T> dimensions(EntityDimensions dimensions) {
		Objects.requireNonNull(dimensions, "Cannot set null dimensions");
		this.dimensions = dimensions;
		return this;
	}

	public KiwiEntityTypeBuilder<T> spawnDimensionsScale(float p_338311_) {
		this.spawnDimensionsScale = p_338311_;
		return this;
	}

	public KiwiEntityTypeBuilder<T> eyeHeight(float p_316663_) {
		this.dimensions = this.dimensions.withEyeHeight(p_316663_);
		return this;
	}

	public KiwiEntityTypeBuilder<T> passengerAttachments(float... p_316352_) {
		for (float f : p_316352_) {
			this.attachments = this.attachments.attach(EntityAttachment.PASSENGER, 0.0F, f, 0.0F);
		}

		return this;
	}

	public KiwiEntityTypeBuilder<T> passengerAttachments(Vec3... p_316160_) {
		for (Vec3 vec3 : p_316160_) {
			this.attachments = this.attachments.attach(EntityAttachment.PASSENGER, vec3);
		}

		return this;
	}

	public KiwiEntityTypeBuilder<T> vehicleAttachment(Vec3 p_316758_) {
		return this.attach(EntityAttachment.VEHICLE, p_316758_);
	}

	public KiwiEntityTypeBuilder<T> ridingOffset(float p_316455_) {
		return this.attach(EntityAttachment.VEHICLE, 0.0F, -p_316455_, 0.0F);
	}

	public KiwiEntityTypeBuilder<T> nameTagOffset(float p_316662_) {
		return this.attach(EntityAttachment.NAME_TAG, 0.0F, p_316662_, 0.0F);
	}

	public KiwiEntityTypeBuilder<T> attach(EntityAttachment p_320654_, float p_320819_, float p_320871_, float p_320278_) {
		this.attachments = this.attachments.attach(p_320654_, p_320819_, p_320871_, p_320278_);
		return this;
	}

	public KiwiEntityTypeBuilder<T> attach(EntityAttachment p_320601_, Vec3 p_320745_) {
		this.attachments = this.attachments.attach(p_320601_, p_320745_);
		return this;
	}

	public KiwiEntityTypeBuilder<T> disableSummon() {
		this.summon = false;
		return this;
	}

	public KiwiEntityTypeBuilder<T> disableSaving() {
		this.serialize = false;
		return this;
	}

	public KiwiEntityTypeBuilder<T> fireImmune() {
		this.fireImmune = true;
		return this;
	}

	public KiwiEntityTypeBuilder<T> specificSpawnBlocks(Block... blocks) {
		this.immuneTo = ImmutableSet.copyOf(blocks);
		return this;
	}

	public KiwiEntityTypeBuilder<T> spawnableFarFromPlayer() {
		this.canSpawnFarFromPlayer = true;
		return this;
	}

	public KiwiEntityTypeBuilder<T> trackRangeChunks(int i) {
		this.clientTrackingRange = i;
		return this;
	}

	public KiwiEntityTypeBuilder<T> trackedUpdateRate(int i) {
		this.updateInterval = i;
		return this;
	}

	public KiwiEntityTypeBuilder<T> requiredFeatures(FeatureFlag... featureFlags) {
		this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
		return this;
	}

	public KiwiEntityTypeBuilder<T> forceTrackedVelocityUpdates(boolean forceTrackedVelocityUpdates) {
		this.forceTrackedVelocityUpdates = forceTrackedVelocityUpdates;
		return this;
	}

	public KiwiEntityTypeBuilder<T> defaultAttributes(Supplier<AttributeSupplier.Builder> defaultAttributeBuilder) {
		Objects.requireNonNull(defaultAttributeBuilder, "Cannot set null attribute builder");
		Preconditions.checkState(type == LivingEntity.class || type == Mob.class, "Only living entities can have default attributes.");
		this.defaultAttributeBuilder = defaultAttributeBuilder;
		return this;
	}

	public KiwiEntityTypeBuilder<T> spawnRestriction(
			SpawnPlacementType location,
			Heightmap.Types heightmap,
			SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
		Preconditions.checkState(type == Mob.class, "Only mobs can have spawn restrictions.");
		this.restrictionLocation = Objects.requireNonNull(location, "Location cannot be null.");
		this.restrictionHeightmap = Objects.requireNonNull(heightmap, "Heightmap type cannot be null.");
		this.spawnPredicate = Objects.requireNonNull(spawnPredicate, "Spawn predicate cannot be null.");
		return this;
	}

	public EntityType<T> build() {
		//		if (type == Entity.class) {
		//			builder = FabricEntityTypeBuilder.create();
		//		} else if (type == LivingEntity.class) {
		//			FabricEntityTypeBuilder.Living<LivingEntity> rawBuilder = FabricEntityTypeBuilder.createLiving();
		//			if (this.defaultAttributeBuilder != null) {
		//				rawBuilder.defaultAttributes(this.defaultAttributeBuilder);
		//			}
		//			builder = (FabricEntityTypeBuilder<T>) rawBuilder;
		//		} else if (type == Mob.class) {
		//			FabricEntityTypeBuilder.Mob<Mob> rawBuilder = FabricEntityTypeBuilder.createMob();
		//			if (this.defaultAttributeBuilder != null) {
		//				rawBuilder.defaultAttributes(this.defaultAttributeBuilder);
		//			}
		//			if (this.spawnPredicate != null) {
		//				rawBuilder.spawnRestriction(this.restrictionLocation, this.restrictionHeightmap, (SpawnPlacements.SpawnPredicate<Mob>) this.spawnPredicate);
		//			}
		//			builder = (FabricEntityTypeBuilder<T>) rawBuilder;
		//		} else {
		//			throw new IllegalStateException("Unknown entity type: " + type);
		//		}
		return new EntityType<>(
				factory,
				category,
				serialize,
				summon,
				fireImmune,
				canSpawnFarFromPlayer,
				immuneTo,
				dimensions.withAttachments(attachments),
				spawnDimensionsScale,
				clientTrackingRange,
				updateInterval,
				requiredFeatures);
	}
}
