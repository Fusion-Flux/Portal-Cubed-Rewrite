package io.github.fusionflux.portalcubed.content.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.fusionflux.portalcubed.content.fizzler.FizzleBehaviour;
import io.github.fusionflux.portalcubed.framework.command.argument.FizzleBehaviourArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class FizzleCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		return literal("fizzle")
				.requires(source -> source.hasPermission(2))
				.then(argument("behaviour", FizzleBehaviourArgumentType.fizzleBehaviour())
						.executes(ctx -> fizzle(ctx, Collections.singleton(ctx.getSource().getEntityOrException())))
						.then(
								argument("targets", EntityArgument.entities())
										.executes(ctx -> fizzle(ctx, EntityArgument.getEntities(ctx, "targets")))
						)
				);
	}

	private static int fizzle(CommandContext<CommandSourceStack> ctx, Collection<? extends Entity> targets) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		FizzleBehaviour behaviour = FizzleBehaviourArgumentType.getFizzleBehaviour(ctx, "behaviour");
		String behaviourTranslationKey = "commands.portalcubed.fizzle." + behaviour.name + ".";

		int successes = Iterables.size(Iterables.filter(targets, behaviour::fizzle));
		int failures = targets.size() - successes;

		if (successes > 0 && failures > 0) {
			source.sendSuccess(() -> Component.translatable(behaviourTranslationKey + "mixed", successes, failures), true);
		} else if (failures > 0) {
			if (failures > 1) {
				source.sendSuccess(() -> Component.translatable(behaviourTranslationKey + "failure.multiple", failures).withStyle(ChatFormatting.RED), true);
			} else {
				source.sendSuccess(() -> Component.translatable(behaviourTranslationKey + "failure", Iterables.getLast(targets).getDisplayName()).withStyle(ChatFormatting.RED), true);
			}
		} else {
			if (successes > 1) {
				source.sendSuccess(() -> Component.translatable(behaviourTranslationKey + "success.multiple", successes), true);
			} else {
				source.sendSuccess(() -> Component.translatable(behaviourTranslationKey + "success", Iterables.getLast(targets).getDisplayName()), true);
			}
		}

		return successes;
	}
}
