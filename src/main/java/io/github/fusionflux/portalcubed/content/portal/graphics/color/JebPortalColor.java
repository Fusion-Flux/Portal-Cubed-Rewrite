package io.github.fusionflux.portalcubed.content.portal.graphics.color;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.color.ColorLerper;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/// @param cycleOffset the offset into the color cycle, in seconds
public record JebPortalColor(float cycleOffset) implements PortalColor {
	public static final StreamCodec<ByteBuf, JebPortalColor> STREAM_CODEC = ByteBufCodecs.FLOAT.map(JebPortalColor::new, JebPortalColor::cycleOffset);
	public static final JebPortalColor DEFAULT = new JebPortalColor(0);
	public static final String PREFIX = "jeb_";

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

	@Override
	public String encode() {
		return this.cycleOffset == 0 ? PREFIX : PREFIX + ':' + this.cycleOffset;
	}

	static JebPortalColor parse(StringReader reader) throws CommandSyntaxException {
		if (!reader.canRead())
			return DEFAULT;

		reader.expect(':');
		return new JebPortalColor(reader.readInt());
	}
}
