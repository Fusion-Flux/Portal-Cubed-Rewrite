package io.github.fusionflux.portalcubed.content.portal;

import io.github.fusionflux.portalcubed.framework.util.DualIterator;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PortalId(String key, Polarity polarity) {
	public static final StreamCodec<ByteBuf, PortalId> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, PortalId::key,
			Polarity.STREAM_CODEC, PortalId::polarity,
			PortalId::new
	);

	public PortalId opposite() {
		return new PortalId(this.key, this.polarity.opposite());
	}

	public MutableComponent component() {
		return Component.translatableEscape("misc.portalcubed.portal_id", this.key, this.polarity.component);
	}

	public static Iterable<PortalId> forPair(String key) {
		return () -> new DualIterator<>(
				new PortalId(key, Polarity.PRIMARY),
				new PortalId(key, Polarity.SECONDARY)
		);
	}
}
