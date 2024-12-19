package io.github.fusionflux.portalcubed.framework.command.argument;

import java.util.Optional;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DataResult;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

/**
 * Formats:
 * - ChatFormatting name (red, dark_green)
 * - 6-digit hex code (#FFFFFF)
 * - integer (1234567)
 */
public class ColorArgumentType implements ArgumentType<Integer> {
	public static final SimpleCommandExceptionType INVALID = new SimpleCommandExceptionType(
			Component.translatable("parsing.portalcubed.color.invalid")
	);

	public static ColorArgumentType color() {
		return new ColorArgumentType();
	}

	public static int getColor(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, int.class);
	}

	@Override
	public Integer parse(StringReader reader) throws CommandSyntaxException {
		// try integer first
		try {
			return reader.readInt();
		} catch (CommandSyntaxException ignored) {}

		// TextColor can handle both hex codes and ChatFormatting
		return readToken(reader)
				.map(TextColor::parseColor)
				.flatMap(DataResult::result)
				.orElseThrow(() -> INVALID.createWithContext(reader))
				.getValue();
	}

	private static Optional<String> readToken(StringReader reader) {
		// Brigadier refuses to read #s with readString for some reason, so this workaround is needed for hex codes
		String remaining = reader.getRemaining();
		if (!remaining.contains(" ")) {
			// no spaces, readStringUntil will fail. Just finish off the string.
			reader.setCursor(reader.getTotalLength());
			return Optional.of(remaining);
		}

		int cursor = reader.getCursor();
		try {
			String string = reader.readStringUntil(' ');
			// parsing expects the separator to still be there
			reader.setCursor(reader.getCursor() - 1);
			return Optional.of(string);
		} catch (CommandSyntaxException ignored) {
			// this will be thrown at end-of-line or invalid escape.
			// In either case, abort reading.
			reader.setCursor(cursor);
			return Optional.empty();
		}
	}
}
