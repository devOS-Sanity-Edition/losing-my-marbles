package one.devos.nautical.losing_my_marbles.framework.extension;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public interface BlockElementRotationExt {
	@Nullable
	Vector3f lmm$angles();

	void lmm$setAngles(Vector3f angles);
}
