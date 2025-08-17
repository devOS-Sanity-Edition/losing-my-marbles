package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record DropLoot(ResourceKey<LootTable> table) implements BounceEffect {
	public static final MapCodec<DropLoot> CODEC = ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").xmap(DropLoot::new, DropLoot::table);

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		if (!(entity.level() instanceof ServerLevel level))
			return;

		LootTable table = level.getServer().reloadableRegistries().getLootTable(this.table);
		if (table == LootTable.EMPTY)
			return;

		LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, entity)
				.withParameter(LootContextParams.ORIGIN, entity.position())
				.create(LootContextParamSets.GIFT); // surprise!

		table.getRandomItems(params, stack -> entity.spawnAtLocation(level, stack));
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
