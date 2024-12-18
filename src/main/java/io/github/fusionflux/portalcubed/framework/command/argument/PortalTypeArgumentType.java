package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.context.CommandContext;

import io.github.fusionflux.portalcubed.content.portal.Polarity;

public class PortalTypeArgumentType extends EnumArgumentType<Polarity> {
	public PortalTypeArgumentType() {
		super(Polarity.class);
	}

	public static PortalTypeArgumentType portalType() {
		return new PortalTypeArgumentType();
	}

	public static Polarity getPortalType(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, Polarity.class);
	}
}
