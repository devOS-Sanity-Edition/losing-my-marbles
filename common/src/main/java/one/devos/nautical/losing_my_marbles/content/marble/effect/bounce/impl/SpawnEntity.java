package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.MapCodec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record SpawnEntity(CompoundTag data) implements BounceEffect {
	public static final MapCodec<SpawnEntity> CODEC = CompoundTag.CODEC.fieldOf("data").xmap(SpawnEntity::new, SpawnEntity::data);

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		if (!(entity.level() instanceof ServerLevel level) || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING))
			return;

		try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LosingMyMarbles.LOGGER)) {
			ValueInput input = TagValueInput.create(reporter, level.registryAccess(), this.data);
			EntityType.create(input, level, EntitySpawnReason.SPAWN_ITEM_USE).ifPresent(created -> {
				created.setPos(entity.position());
				level.addFreshEntity(created);
			});
		}
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
