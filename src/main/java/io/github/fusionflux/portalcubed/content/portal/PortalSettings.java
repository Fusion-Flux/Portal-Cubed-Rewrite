package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PortalSettings(int color, PortalShape shape, boolean render, boolean validate) {
	public static final Codec<PortalSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("color").forGetter(PortalSettings::colorNoAlpha),
			PortalShape.CODEC.fieldOf("shape").forGetter(PortalSettings::shape),
			Codec.BOOL.fieldOf("render").forGetter(PortalSettings::render),
			Codec.BOOL.fieldOf("validate").forGetter(PortalSettings::validate)
	).apply(instance, PortalSettings::new));

	public static final StreamCodec<ByteBuf, PortalSettings> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, PortalSettings::colorNoAlpha,
			PortalShape.STREAM_CODEC, PortalSettings::shape,
			ByteBufCodecs.BOOL, PortalSettings::render,
			ByteBufCodecs.BOOL, PortalSettings::validate,
			PortalSettings::new
	);

	public static final PortalSettings DEFAULT_PRIMARY = new PortalSettings(Polarity.PRIMARY.defaultColor, PortalShape.SQUARE);
	public static final PortalSettings DEFAULT_SECONDARY = new PortalSettings(Polarity.SECONDARY.defaultColor, PortalShape.SQUARE);

	public PortalSettings(int color, PortalShape shape, boolean render, boolean validate) {
		this.color = fixAlpha(color);
		this.shape = shape;
		this.render = render;
		this.validate = validate;
	}

	public PortalSettings(int color, PortalShape shape) {
		this(color, shape, true, true);
	}

	public PortalSettings withColor(int color) {
		return new PortalSettings(color, this.shape, this.render, this.validate);
	}

	public PortalSettings withShape(PortalShape shape) {
		return new PortalSettings(this.color, shape, this.render, this.validate);
	}

	public PortalSettings withRender(boolean render) {
		return new PortalSettings(this.color, this.shape, render, this.validate);
	}

	public PortalSettings withValidate(boolean validate) {
		return new PortalSettings(this.color, this.shape, this.render, validate);
	}

	public int colorNoAlpha() {
		return this.color & 0x00FFFFFF;
	}

	public static int fixAlpha(int color) {
		return color | 0xFF000000; // require alpha 255
	}
}
