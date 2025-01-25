package io.github.fusionflux.portalcubed.content.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.fizzler.FizzleBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;

public class FizzleCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
		return literal("fizzle")
				.requires(source -> source.hasPermission(2))
				.then(argument("behaviour", ResourceArgument.resource(buildContext, PortalCubedRegistries.FIZZLE_BEHAVIOUR.key()))
						.executes(ctx -> fizzle(ctx, Collections.singleton(ctx.getSource().getEntityOrException())))
						.then(
								argument("targets", EntityArgument.entities())
										.executes(ctx -> fizzle(ctx, EntityArgument.getEntities(ctx, "targets")))
						)
				);
	}

	private static int fizzle(CommandContext<CommandSourceStack> ctx, Collection<? extends Entity> targets) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		@SuppressWarnings("unchecked")
		Holder.Reference<FizzleBehaviour> behaviour = ResourceArgument.getResource(ctx, "behaviour", (ResourceKey<Registry<FizzleBehaviour>>) PortalCubedRegistries.FIZZLE_BEHAVIOUR.key());
		String behaviourTranslationKey = "commands.portalcubed.fizzle." + behaviour.key().location().getPath() + ".";

		int successes = Iterables.size(Iterables.filter(targets, behaviour.value()::fizzle));
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
