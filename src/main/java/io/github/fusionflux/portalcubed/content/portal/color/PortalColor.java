package io.github.fusionflux.portalcubed.content.portal.color;

import java.util.Locale;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringRepresentable;

public interface PortalColor {
	Codec<PortalColor> CODEC = Type.CODEC.dispatch(PortalColor::type, type -> type.codec);
	StreamCodec<ByteBuf, PortalColor> STREAM_CODEC = Type.STREAM_CODEC.dispatch(PortalColor::type, type -> type.streamCodec);

	int get(float ticks);

	@ApiStatus.NonExtendable
	default int getOpaque(float ticks) {
		return ARGB.opaque(this.get(ticks));
	}

	Type type();

	@Override
	boolean equals(Object o);

	@FunctionalInterface
	interface CommandParser {
		PortalColor parse(StringReader reader) throws CommandSyntaxException;
	}

	enum Type implements StringRepresentable {
		CONSTANT(ConstantPortalColor.CODEC, ConstantPortalColor.STREAM_CODEC, ConstantPortalColor::parse),
		JEB(JebPortalColor.CODEC, JebPortalColor.STREAM_CODEC, JebPortalColor::parse);

		public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
		public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = PortalCubedStreamCodecs.ofEnum(Type.class);

		public final String name;
		public final MapCodec<? extends PortalColor> codec;
		public final StreamCodec<ByteBuf, ? extends PortalColor> streamCodec;
		public final CommandParser parser;

		Type(MapCodec<? extends PortalColor> codec, StreamCodec<ByteBuf, ? extends PortalColor> streamCodec, CommandParser parser) {
			this.name = this.name().toLowerCase(Locale.ROOT);
			this.codec = codec;
			this.streamCodec = streamCodec;
			this.parser = parser;
		}

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}
}
