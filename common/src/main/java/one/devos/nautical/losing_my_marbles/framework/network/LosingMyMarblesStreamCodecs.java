package one.devos.nautical.losing_my_marbles.framework.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;

public final class LosingMyMarblesStreamCodecs {
	public static final StreamCodec<ByteBuf, float[]> FLOAT_ARRAY = new StreamCodec<>() {
		@Override
		public float[] decode(ByteBuf buf) {
			int size = VarInt.read(buf);
			float[] array = new float[size];
			for (int i = 0; i < size; i++) {
				array[i] = buf.readFloat();
			}
			return array;
		}

		@Override
		public void encode(ByteBuf buf, float[] array) {
			VarInt.write(buf, array.length);
			for (float f : array) {
				buf.writeFloat(f);
			}
		}
	};
}
