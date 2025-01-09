package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.context.CommandContext;

import io.github.fusionflux.portalcubed.content.portal.Polarity;

public class PolarityArgumentType extends EnumArgumentType<Polarity> {
	public PolarityArgumentType() {
		super(Polarity.class);
	}

	public static PolarityArgumentType polarity() {
		return new PolarityArgumentType();
	}

	public static Polarity getPolarity(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, Polarity.class);
	}
}
