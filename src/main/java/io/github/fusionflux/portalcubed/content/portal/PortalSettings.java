package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;

public record PortalSettings(int color, PortalShape shape) {
	public static final Codec<PortalSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("color").forGetter(PortalSettings::color),
			PortalShape.CODEC.fieldOf("shape").forGetter(PortalSettings::shape)
	).apply(instance, PortalSettings::new));

	public static final PortalSettings DEFAULT_PRIMARY = new PortalSettings(PortalType.PRIMARY.defaultColor, PortalShape.SQUARE);
	public static final PortalSettings DEFAULT_SECONDARY = new PortalSettings(PortalType.SECONDARY.defaultColor, PortalShape.SQUARE);

	public static void toNetwork(FriendlyByteBuf buf, PortalSettings data) {
		buf.writeVarInt(data.color);
		buf.writeEnum(data.shape);
	}

	public static PortalSettings fromNetwork(FriendlyByteBuf buf) {
		int color = buf.readVarInt();
		PortalShape shape = buf.readEnum(PortalShape.class);
		return new PortalSettings(color, shape);
	}
}
