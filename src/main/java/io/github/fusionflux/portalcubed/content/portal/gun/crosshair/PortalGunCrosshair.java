package io.github.fusionflux.portalcubed.content.portal.gun.crosshair;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.fusionflux.portalcubed.PortalCubed;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record PortalGunCrosshair(ResourceKey<PortalGunCrosshairType> typeId, boolean enableLastPlaced) {
	public static final Codec<PortalGunCrosshair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceKey.codec(PortalGunCrosshairType.REGISTRY_KEY).fieldOf("type").forGetter(PortalGunCrosshair::typeId),
			Codec.BOOL.fieldOf("enable_last_placed").forGetter(PortalGunCrosshair::enableLastPlaced)
	).apply(instance, PortalGunCrosshair::new));
	public static final StreamCodec<ByteBuf, PortalGunCrosshair> STREAM_CODEC = StreamCodec.composite(
			ResourceKey.streamCodec(PortalGunCrosshairType.REGISTRY_KEY), PortalGunCrosshair::typeId,
			ByteBufCodecs.BOOL, PortalGunCrosshair::enableLastPlaced,
			PortalGunCrosshair::new
	);

	public static final PortalGunCrosshair DEFAULT = new PortalGunCrosshair(ResourceKey.create(PortalGunCrosshairType.REGISTRY_KEY, PortalCubed.id("round")), true);
}
