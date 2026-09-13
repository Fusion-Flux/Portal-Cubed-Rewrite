package io.github.fusionflux.portalcubed.content.portal.graphics.color;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import io.github.fusionflux.portalcubed.mixin.portals.TextColorAccessor;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.commands.arguments.HexColorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;

public record ConstantPortalColor(int color) implements PortalColor {
	public static final StreamCodec<ByteBuf, ConstantPortalColor> STREAM_CODEC = ByteBufCodecs.INT.map(ConstantPortalColor::new, ConstantPortalColor::color);

	public static final SimpleCommandExceptionType INVALID = new SimpleCommandExceptionType(
			Component.translatable("parsing.portalcubed.portal_color.invalid")
	);

	// map of named TextColors by (transparent) RGB values
	private static final Int2ReferenceMap<TextColor> namedColorsByValue = Util.make(new Int2ReferenceOpenHashMap<>(), map -> {
		for (TextColor color : TextColorAccessor.getNAMED_COLORS().values()) {
			map.put(color.getValue(), color);
		}
	});

	private static final HexColorArgument dummyHexColor = HexColorArgument.hexColor();

	@Override
	public int get(float partialTicks) {
		return this.color;
	}

	@Override
	public Type type() {
		return Type.CONSTANT;
	}

	@Override
	public String encode() {
		int color = ARGB.transparent(this.color);
		TextColor named = namedColorsByValue.get(color);
		if (named != null) {
			return named.serialize();
		}

		return String.format(Locale.ROOT, "#%06X", color);
	}

	static ConstantPortalColor parse(StringReader reader) throws CommandSyntaxException {
		if (reader.peek() == '#') {
			// read a hex color
			reader.skip();
			int color = dummyHexColor.parse(reader);
			return new ConstantPortalColor(color);
		}

		// try a named one
		int cursor = reader.getCursor();
		String name = reader.readUnquotedString();
		TextColor color = TextColorAccessor.getNAMED_COLORS().get(name);
		if (color != null) {
			return new ConstantPortalColor(color.getValue());
		}

		// try an integer
		try {
			reader.setCursor(cursor);
			return new ConstantPortalColor(reader.readInt());
		} catch (CommandSyntaxException ignored) {}

		// all failed
		throw INVALID.createWithContext(reader);
	}
}
