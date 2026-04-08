package snownee.kiwi.customization.block.soundtype;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import snownee.kiwi.util.DeferredHolder;

public class DeferredSoundType extends SoundType {
	private static final DeferredHolder<SoundEvent, SoundEvent> EMPTY_SOUND_EVENT = DeferredHolder.create(
			Registries.SOUND_EVENT,
			SoundEvents.EMPTY.location());

	public static final MapCodec<DeferredSoundType> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("volume", 1.0f).forGetter(SoundType::getVolume),
			Codec.FLOAT.optionalFieldOf("pitch", 1.0f).forGetter(SoundType::getPitch),
			DeferredHolder.codec(Registries.SOUND_EVENT).optionalFieldOf("break", EMPTY_SOUND_EVENT).forGetter(it -> it.breakSound),
			DeferredHolder.codec(Registries.SOUND_EVENT).optionalFieldOf("step", EMPTY_SOUND_EVENT).forGetter(it -> it.stepSound),
			DeferredHolder.codec(Registries.SOUND_EVENT).optionalFieldOf("place", EMPTY_SOUND_EVENT).forGetter(it -> it.placeSound),
			DeferredHolder.codec(Registries.SOUND_EVENT).optionalFieldOf("hit", EMPTY_SOUND_EVENT).forGetter(it -> it.hitSound),
			DeferredHolder.codec(Registries.SOUND_EVENT).optionalFieldOf("fall", EMPTY_SOUND_EVENT).forGetter(it -> it.fallSound)
	).apply(instance, DeferredSoundType::new));

	private final DeferredHolder<SoundEvent, SoundEvent> breakSound;
	private final DeferredHolder<SoundEvent, SoundEvent> stepSound;
	private final DeferredHolder<SoundEvent, SoundEvent> placeSound;
	private final DeferredHolder<SoundEvent, SoundEvent> hitSound;
	private final DeferredHolder<SoundEvent, SoundEvent> fallSound;

	public DeferredSoundType(
			float volumeIn,
			float pitchIn,
			DeferredHolder<SoundEvent, SoundEvent> breakSoundIn,
			DeferredHolder<SoundEvent, SoundEvent> stepSoundIn,
			DeferredHolder<SoundEvent, SoundEvent> placeSoundIn,
			DeferredHolder<SoundEvent, SoundEvent> hitSoundIn,
			DeferredHolder<SoundEvent, SoundEvent> fallSoundIn) {
		super(volumeIn, pitchIn, null, null, null, null, null);
		this.breakSound = breakSoundIn;
		this.stepSound = stepSoundIn;
		this.placeSound = placeSoundIn;
		this.hitSound = hitSoundIn;
		this.fallSound = fallSoundIn;
	}

	@Override
	public SoundEvent getBreakSound() {
		return breakSound.get();
	}

	@Override
	public SoundEvent getStepSound() {
		return stepSound.get();
	}

	@Override
	public SoundEvent getPlaceSound() {
		return placeSound.get();
	}

	@Override
	public SoundEvent getHitSound() {
		return hitSound.get();
	}

	@Override
	public SoundEvent getFallSound() {
		return fallSound.get();
	}
}
