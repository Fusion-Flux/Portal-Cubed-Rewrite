package io.github.fusionflux.portalcubed.framework.command.argument;

import org.joml.Quaternionf;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.network.chat.Component;

public class QuaternionArgumentType implements ArgumentType<Quaternionf> {
	public static final SimpleCommandExceptionType INCOMPLETE = new SimpleCommandExceptionType(
			Component.translatable("parsing.portalcubed.quaternion.incomplete")
	);

	public static QuaternionArgumentType quaternion() {
		return new QuaternionArgumentType();
	}

	public static Quaternionf getQuaternion(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, Quaternionf.class);
	}

	@Override
	public Quaternionf parse(StringReader reader) throws CommandSyntaxException {
		int cursor = reader.getCursor();
		float x = reader.readFloat();
		if (reader.canRead() && reader.peek() == ' ') {
			reader.skip();
			float y = reader.readFloat();
			if (reader.canRead() && reader.peek() == ' ') {
				reader.skip();
				float z = reader.readFloat();
				if (reader.canRead() && reader.peek() == ' ') {
					reader.skip();
					float w = reader.readFloat();
					return new Quaternionf(x, y, z, w);
				}
			}
		}

		reader.setCursor(cursor);
		throw INCOMPLETE.createWithContext(reader);
	}
}
