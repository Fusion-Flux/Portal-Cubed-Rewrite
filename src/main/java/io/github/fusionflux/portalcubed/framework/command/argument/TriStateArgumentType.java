package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.util.TriState;

public class TriStateArgumentType extends EnumArgumentType<TriState> {
	public TriStateArgumentType() {
		super(TriState.class);
	}

	public static TriStateArgumentType triState() {
		return new TriStateArgumentType();
	}

	public static TriState getTriState(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, TriState.class);
	}
}
