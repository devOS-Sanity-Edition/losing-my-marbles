package one.devos.nautical.losing_my_marbles.content;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import one.devos.nautical.losing_my_marbles.content.command.RequestGeometryCommand;

public final class LosingMyMarblesCommands {
	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		return literal("losing_my_marbles")
				// if we ever add non-op commands, remove this requirement.
				.requires(source -> source.hasPermission(2))
				.then(RequestGeometryCommand.build());
	}
}
