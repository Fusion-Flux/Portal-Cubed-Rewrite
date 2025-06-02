package io.github.fusionflux.portalcubed.content.portal.color;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.framework.command.argument.ColorArgumentType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record ConstantPortalColor(int color) implements PortalColor {
	public static final MapCodec<ConstantPortalColor> CODEC = ExtraCodecs.RGB_COLOR_CODEC.fieldOf("value").xmap(ConstantPortalColor::new, ConstantPortalColor::color);
	public static final StreamCodec<ByteBuf, ConstantPortalColor> STREAM_CODEC = ByteBufCodecs.INT.map(ConstantPortalColor::new, ConstantPortalColor::color);

	private static final ColorArgumentType dummyColor = ColorArgumentType.color();

	@Override
	public int get(float partialTicks) {
		return this.color;
	}

	@Override
	public Type type() {
		return Type.CONSTANT;
	}

	public static ConstantPortalColor parse(StringReader reader) throws CommandSyntaxException {
		reader.skipWhitespace();
		int color = dummyColor.parse(reader);
		return new ConstantPortalColor(color);
	}
}
