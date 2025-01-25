package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.context.CommandContext;

import io.github.fusionflux.portalcubed.content.fizzler.FizzleBehaviour;

public class FizzleBehaviourArgumentType extends EnumArgumentType<FizzleBehaviour> {
	public FizzleBehaviourArgumentType() {
		super(FizzleBehaviour.class);
	}

	public static FizzleBehaviourArgumentType fizzleBehaviour() {
		return new FizzleBehaviourArgumentType();
	}

	public static FizzleBehaviour getFizzleBehaviour(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, FizzleBehaviour.class);
	}
}
