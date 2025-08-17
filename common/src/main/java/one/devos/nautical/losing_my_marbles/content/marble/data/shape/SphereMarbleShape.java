package one.devos.nautical.losing_my_marbles.content.marble.data.shape;

import com.github.stephengold.joltjni.SphereShape;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.framework.network.LosingMyMarblesStreamCodecs;

public record SphereMarbleShape(float radius) implements MarbleShape {
	public static final SphereMarbleShape DEFAULT = new SphereMarbleShape(3 / 16f);

	public static final MapCodec<SphereMarbleShape> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.floatRange(1 / 16f, 2).fieldOf("radius").forGetter(SphereMarbleShape::radius)
	).apply(i, SphereMarbleShape::new));

	public static final StreamCodec<ByteBuf, SphereMarbleShape> STREAM_CODEC = LosingMyMarblesStreamCodecs.floatRange(1 / 16f, 2)
			.map(SphereMarbleShape::new, SphereMarbleShape::radius);

	public static final Type<SphereMarbleShape> TYPE = new Type<>(CODEC, STREAM_CODEC);

	@Override
	public CreatedShape createJoltShape(float scale) {
		float actualRadius = this.radius * scale;
		SphereShape sphere = new SphereShape(actualRadius);
		sphere.setDensity(DENSITY);
		return new CreatedShape(sphere, new Vec3(0, actualRadius, 0));
	}

	@Override
	public Type<?> type() {
		return TYPE;
	}
}
