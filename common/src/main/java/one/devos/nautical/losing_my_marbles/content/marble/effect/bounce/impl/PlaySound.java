package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;
import one.devos.nautical.losing_my_marbles.framework.data.LosingMyMarblesCodecs;

public record PlaySound(Holder<SoundEvent> sound, float volume, float pitch, boolean scaleWithVelocity) implements BounceEffect {
	public static final MapCodec<PlaySound> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			SoundEvent.CODEC.fieldOf("sound").forGetter(PlaySound::sound),
			LosingMyMarblesCodecs.NON_ZERO_NORMALIZED_FLOAT.optionalFieldOf("volume", 1f).forGetter(PlaySound::volume),
			LosingMyMarblesCodecs.POSITIVE_FLOAT.optionalFieldOf("pitch", 1f).forGetter(PlaySound::pitch),
			Codec.BOOL.optionalFieldOf("scale_with_velocity", true).forGetter(PlaySound::scaleWithVelocity)
	).apply(i, PlaySound::new));

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		if (!this.scaleWithVelocity) {
			entity.playSound(this.sound.value(), this.volume, this.pitch);
			return;
		}

		Vec3 delta = oldVel.vectorTo(newVel);
		float scale = Math.clamp((float) delta.length(), 0.1f, 1f);
		// nonsense that seems kinda okay
		float pitch = this.pitch - scale / 3;
		entity.playSound(this.sound.value(), this.volume, pitch);
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
