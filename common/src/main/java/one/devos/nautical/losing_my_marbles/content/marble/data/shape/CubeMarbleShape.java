package one.devos.nautical.losing_my_marbles.content.marble.data.shape;

import com.github.stephengold.joltjni.BoxShape;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.framework.network.LosingMyMarblesStreamCodecs;

public record CubeMarbleShape(float sideLength) implements MarbleShape {
	public static final MapCodec<CubeMarbleShape> CODEC = Codec.floatRange(1 / 16f, 2)
			.fieldOf("side_length")
			.xmap(CubeMarbleShape::new, CubeMarbleShape::sideLength);

	public static final StreamCodec<ByteBuf, CubeMarbleShape> STREAM_CODEC = LosingMyMarblesStreamCodecs.floatRange(1 / 8f, 4)
			.map(CubeMarbleShape::new, CubeMarbleShape::sideLength);

	public static final Type<CubeMarbleShape> TYPE = new Type<>(CODEC, STREAM_CODEC);

	@Override
	public CreatedShape createJoltShape(float scale) {
		float halfExtent = (this.sideLength / 2) * scale;
		BoxShape cube = new BoxShape(halfExtent);
		cube.setDensity(DENSITY);
		return new CreatedShape(cube, new Vec3(0, halfExtent, 0));
	}

	@Override
	public Type<?> type() {
		return TYPE;
	}
}
