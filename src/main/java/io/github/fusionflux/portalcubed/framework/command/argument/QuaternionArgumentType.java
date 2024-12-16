package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.joml.Quaternionf;

public class QuaternionArgumentType implements ArgumentType<Quaternionf> {
	public static QuaternionArgumentType quaternion() {
		return new QuaternionArgumentType();
	}

	public static Quaternionf getQuaternion(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, Quaternionf.class);
	}

	@Override
	public Quaternionf parse(StringReader reader) throws CommandSyntaxException {
		float x = reader.readFloat();
		float y = reader.readFloat();
		float z = reader.readFloat();
		float w = reader.readFloat();
		return new Quaternionf(x, y, z, w);
	}
}
