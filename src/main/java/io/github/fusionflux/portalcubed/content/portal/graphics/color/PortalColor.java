package io.github.fusionflux.portalcubed.content.portal.graphics.color;

import java.util.Locale;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringRepresentable;

public interface PortalColor {
	Codec<PortalColor> CODEC = Codec.STRING.comapFlatMap(PortalColor::decode, PortalColor::encode);
	StreamCodec<ByteBuf, PortalColor> STREAM_CODEC = Type.STREAM_CODEC.dispatch(PortalColor::type, type -> type.streamCodec);

	int get(float ticks);

	@ApiStatus.NonExtendable
	default int getOpaque(float ticks) {
		return ARGB.opaque(this.get(ticks));
	}

	Type type();

	String encode();

	@Override
	boolean equals(Object o);

	static DataResult<PortalColor> decode(String string) {
		StringReader reader = new StringReader(string);

		try {
			return DataResult.success(parse(reader));
		} catch (CommandSyntaxException e) {
			return DataResult.error(e::getMessage);
		}
	}

	static PortalColor parse(StringReader reader) throws CommandSyntaxException {
		reader.skipWhitespace();
		Type type = Type.CONSTANT;

		if (reader.getRemaining().startsWith(JebPortalColor.PREFIX)) {
			reader.setCursor(reader.getCursor() + JebPortalColor.PREFIX.length());
			type = Type.JEB;
		}

		return type.parser.parse(reader);
	}

	@FunctionalInterface
	interface CommandParser {
		PortalColor parse(StringReader reader) throws CommandSyntaxException;
	}

	enum Type implements StringRepresentable {
		CONSTANT(ConstantPortalColor.STREAM_CODEC, ConstantPortalColor::parse),
		JEB(JebPortalColor.STREAM_CODEC, JebPortalColor::parse);

		public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
		public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = PortalCubedStreamCodecs.ofEnum(Type.class);

		private final String name;
		private final StreamCodec<ByteBuf, ? extends PortalColor> streamCodec;
		private final CommandParser parser;

		Type(StreamCodec<ByteBuf, ? extends PortalColor> streamCodec, CommandParser parser) {
			this.name = this.name().toLowerCase(Locale.ROOT);
			this.streamCodec = streamCodec;
			this.parser = parser;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}
}
