package one.devos.nautical.losing_my_marbles.content.marble.data.shape;

import com.github.stephengold.joltjni.ShapeResult;
import com.github.stephengold.joltjni.TaperedCapsuleShapeSettings;
import com.github.stephengold.joltjni.Vec3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import one.devos.nautical.losing_my_marbles.framework.network.LosingMyMarblesStreamCodecs;

public record TaperedCapsuleMarbleShape(float bottomRadius, float topRadius, float height) implements MarbleShape {
	public static final MapCodec<TaperedCapsuleMarbleShape> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.floatRange(1 / 16f, 2).fieldOf("bottom_radius").forGetter(TaperedCapsuleMarbleShape::bottomRadius),
			Codec.floatRange(1 / 16f, 2).fieldOf("top_radius").forGetter(TaperedCapsuleMarbleShape::topRadius),
			Codec.floatRange(1 / 16f, 2).fieldOf("height").forGetter(TaperedCapsuleMarbleShape::height)
	).apply(i, TaperedCapsuleMarbleShape::new));

	public static final StreamCodec<ByteBuf, TaperedCapsuleMarbleShape> STREAM_CODEC = StreamCodec.composite(
			LosingMyMarblesStreamCodecs.floatRange(1 / 16f, 2), TaperedCapsuleMarbleShape::bottomRadius,
			LosingMyMarblesStreamCodecs.floatRange(1 / 16f, 2), TaperedCapsuleMarbleShape::topRadius,
			LosingMyMarblesStreamCodecs.floatRange(1 / 16f, 2), TaperedCapsuleMarbleShape::height,
			TaperedCapsuleMarbleShape::new
	);

	public static final Type<TaperedCapsuleMarbleShape> TYPE = new Type<>(CODEC, STREAM_CODEC);

	@Override
	public CreatedShape createJoltShape(float scale) {
		TaperedCapsuleShapeSettings settings = new TaperedCapsuleShapeSettings();
		settings.setBottomRadius(this.bottomRadius * scale);
		settings.setTopRadius(this.topRadius * scale);
		settings.setHalfHeightOfTaperedCylinder(this.height / 2 * scale);
		settings.setDensity(DENSITY);

		try (ShapeResult result = settings.create()) {
			settings.toRef().close();
			float yOffset = this.bottomRadius + (this.height / 2) * scale;
			return new CreatedShape(result.get(), new Vec3(0, yOffset, 0));
		}
	}

	@Override
	public Type<?> type() {
		return TYPE;
	}
}
