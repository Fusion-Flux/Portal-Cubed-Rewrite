package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.context.CommandContext;

import io.github.fusionflux.portalcubed.content.portal.PortalType;

public class PortalTypeArgumentType extends EnumArgumentType<PortalType> {
	public PortalTypeArgumentType() {
		super(PortalType.class);
	}

	public static PortalTypeArgumentType portalType() {
		return new PortalTypeArgumentType();
	}

	public static PortalType getPortalType(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, PortalType.class);
	}
}
