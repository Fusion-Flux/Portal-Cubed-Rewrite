package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.context.CommandContext;

import io.github.fusionflux.portalcubed.content.portal.PortalShape;

public class PortalShapeArgumentType extends EnumArgumentType<PortalShape> {
	public PortalShapeArgumentType() {
		super(PortalShape.class);
	}

	public static PortalShapeArgumentType shape() {
		return new PortalShapeArgumentType();
	}

	public static PortalShape getShape(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, PortalShape.class);
	}
}
