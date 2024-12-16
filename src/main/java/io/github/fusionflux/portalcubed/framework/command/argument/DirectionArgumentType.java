package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.core.Direction;

public class DirectionArgumentType extends EnumArgumentType<Direction> {
	public DirectionArgumentType() {
		super(Direction.class);
	}

	public static DirectionArgumentType direction() {
		return new DirectionArgumentType();
	}

	public static Direction getDirection(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, Direction.class);
	}
}
