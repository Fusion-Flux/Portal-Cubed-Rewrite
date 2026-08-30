package io.github.fusionflux.portalcubed.content.portal.graphics.color;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.color.ColorLerper;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/// @param cycleOffset the offset into the color cycle, in seconds
public record JebPortalColor(float cycleOffset) implements PortalColor {
	public static final MapCodec<JebPortalColor> CODEC = Codec.FLOAT.optionalFieldOf("cycle_offset", 0f).xmap(JebPortalColor::new, JebPortalColor::cycleOffset);
	public static final StreamCodec<ByteBuf, JebPortalColor> STREAM_CODEC = ByteBufCodecs.FLOAT.map(JebPortalColor::new, JebPortalColor::cycleOffset);

	@Override
	public int get(float ticks) {
		// seconds -> ticks
		float offset = this.cycleOffset * 20;
		return ColorLerper.getLerpedColor(ColorLerper.Type.SHEEP, ticks + offset);
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
