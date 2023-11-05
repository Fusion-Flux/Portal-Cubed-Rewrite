package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;

public record PortalData(int color, PortalShape shape) {
	public static final Codec<PortalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("color").forGetter(PortalData::color),
			PortalShape.CODEC.fieldOf("shape").forGetter(PortalData::shape)
	).apply(instance, PortalData::new));

	public static final PortalData DEFAULT_PRIMARY = new PortalData(PortalType.PRIMARY.defaultColor, PortalShape.SQUARE);
	public static final PortalData DEFAULT_SECONDARY = new PortalData(PortalType.SECONDARY.defaultColor, PortalShape.SQUARE);
	// for entity synced data, default needs to be a value that will never be set otherwise
	public static final PortalData INVALID = new PortalData(-1, PortalShape.SQUARE);

	public static void toNetwork(FriendlyByteBuf buf, PortalData data) {
		buf.writeVarInt(data.color);
		buf.writeEnum(data.shape);
	}

	public static PortalData fromNetwork(FriendlyByteBuf buf) {
		int color = buf.readVarInt();
		PortalShape shape = buf.readEnum(PortalShape.class);
		return new PortalData(color, shape);
	}
}
