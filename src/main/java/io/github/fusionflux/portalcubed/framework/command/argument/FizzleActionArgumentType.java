package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.context.CommandContext;

import io.github.fusionflux.portalcubed.content.fizzler.FizzleAction;

public class FizzleActionArgumentType extends EnumArgumentType<FizzleAction> {
	public FizzleActionArgumentType() {
		super(FizzleAction.class);
	}

	public static FizzleActionArgumentType fizzleAction() {
		return new FizzleActionArgumentType();
	}

	public static FizzleAction getFizzleAction(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, FizzleAction.class);
	}
}
