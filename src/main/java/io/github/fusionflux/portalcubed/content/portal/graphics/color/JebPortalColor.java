package io.github.fusionflux.portalcubed.content.portal.graphics.color;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.color.ColorLerper;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record JebPortalColor(int colorOffset) implements PortalColor {
	public static final MapCodec<JebPortalColor> CODEC = Codec.INT.optionalFieldOf("color_offset", 0).xmap(JebPortalColor::new, JebPortalColor::colorOffset);
	public static final StreamCodec<ByteBuf, JebPortalColor> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(JebPortalColor::new, JebPortalColor::colorOffset);

	@Override
	public int get(float ticks) {
		return ColorLerper.getLerpedColor(ColorLerper.Type.SHEEP, ticks);
	}

	@Override
	public Type type() {
		return Type.JEB;
	}

	public static JebPortalColor parse(StringReader reader) {
		reader.skipWhitespace();
		try {
			return new JebPortalColor(reader.readInt());
		} catch (CommandSyntaxException ignored) {
			return new JebPortalColor(0);
		}
	}
}
