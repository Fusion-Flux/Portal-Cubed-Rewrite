package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.network.chat.Component;

/**
 * Formats:
 * - ChatFormatting name (red, lime_green) ({@link ColorArgument})
 * - hex code (#FFFFFF)
 * - integer (1234567)
 */
public class ColorArgumentType implements ArgumentType<Integer> {
	public static final SimpleCommandExceptionType INVALID = new SimpleCommandExceptionType(
			Component.translatable("parsing.portalcubed.color.invalid")
	);

	private static final ColorArgument vanillaDummy = ColorArgument.color();

	public static ColorArgumentType color() {
		return new ColorArgumentType();
	}

	public static int getColor(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, int.class);
	}

	@Override
	public Integer parse(StringReader reader) throws CommandSyntaxException {
		int cursor = reader.getCursor();

		if (reader.canRead() && reader.peek() == '#') {
			try {
				reader.skip(); // skip the #
				return Integer.parseInt(reader.readString(), 16);
			} catch (CommandSyntaxException | NumberFormatException ignored) {
				reader.setCursor(cursor);
				throw INVALID.createWithContext(reader);
			}
		}

		try {
			return reader.readInt();
		} catch (CommandSyntaxException ignored) {} // don't need to reset cursor here, readInt does it

		try {
			ChatFormatting formatting = vanillaDummy.parse(reader);
			return formatting.getColor();
		} catch (CommandSyntaxException ignored) {
			reader.setCursor(cursor);
		}

		// all formats failed.
		throw INVALID.createWithContext(reader);
	}
}
