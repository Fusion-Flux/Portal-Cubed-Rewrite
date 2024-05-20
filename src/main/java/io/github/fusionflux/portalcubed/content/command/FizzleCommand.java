package io.github.fusionflux.portalcubed.content.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import com.mojang.brigadier.context.CommandContext;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.fusionflux.portalcubed.content.fizzler.FizzleBehaviour;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;

import net.minecraft.world.entity.Entity;

import org.quiltmc.qsl.command.api.EnumArgumentType;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class FizzleCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		return literal("fizzle")
				.requires(source -> source.hasPermission(2))
				.then(argument("behaviour", EnumArgumentType.enumConstant(FizzleBehaviour.class))
						.executes(ctx -> fizzle(ctx, Collections.singleton(ctx.getSource().getEntityOrException())))
						.then(
								argument("targets", EntityArgument.entities())
										.executes(ctx -> fizzle(ctx, EntityArgument.getEntities(ctx, "targets")))
						)
				);
	}

	private static int fizzle(CommandContext<CommandSourceStack> ctx, Collection<? extends Entity> targets) throws CommandSyntaxException {
		FizzleBehaviour behaviour = EnumArgumentType.getEnumConstant(ctx, "behaviour", FizzleBehaviour.class);
		targets.forEach(behaviour::fizzle);
		return targets.size();
	}
}
