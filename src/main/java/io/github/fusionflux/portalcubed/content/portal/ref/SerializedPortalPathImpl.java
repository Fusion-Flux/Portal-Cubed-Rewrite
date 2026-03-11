package io.github.fusionflux.portalcubed.content.portal.ref;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.serialization.DataResult;

import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

record SerializedPortalPathImpl(List<Entry> entries) implements PortalPath.Serialized {
	public static final StreamCodec<ByteBuf, PortalPath.Serialized> STREAM_CODEC = Entry.STREAM_CODEC.apply(ByteBufCodecs.list()).map(
			SerializedPortalPathImpl::create, path -> ((SerializedPortalPathImpl) path).entries()
	);

	SerializedPortalPathImpl {
		checkSize(entries, RuntimeException::new);
	}

	@Override
	public DataResult<PortalPath> resolve(PortalManager manager) {
		List<PortalPath.Entry> entries = new ArrayList<>();

		for (Entry entry : this.entries) {
			PortalReference entered = manager.getPortal(entry.enteredId);
			if (entered == null) {
				return DataResult.error(() -> "Portal " + entry.enteredId + " doesn't exist");
			}

			PortalReference exited = manager.getPortal(entry.exitedId);
			if (exited == null) {
				return DataResult.error(() -> "Portal " + entry.exitedId + " doesn't exist");
			}

			entries.add(new PortalPath.Entry(
					new HitPortal(entered, entry.enteredPos),
					new HitPortal(exited, entry.exitedPos)
			));
		}

		return DataResult.success(PortalPath.of(entries));
	}

	static PortalPath.Serialized of(PortalPath path) {
		List<Entry> entries = new ArrayList<>();

		for (PortalPath.Entry entry : path.entries()) {
			entries.add(new Entry(
					entry.entered().reference().id, entry.entered().pos(),
					entry.exited().reference().id, entry.exited().pos()
			));
		}

		return new SerializedPortalPathImpl(entries);
	}

	private static PortalPath.Serialized create(List<Entry> entries) throws DecoderException {
		checkSize(entries, DecoderException::new);
		return new SerializedPortalPathImpl(entries);
	}

	private static <X extends Throwable> void checkSize(List<Entry> entries, Function<String, X> exceptionFactory) throws X {
		if (entries.isEmpty()) {
			throw exceptionFactory.apply("Cannot create a path with no entries");
		}
	}

	private record Entry(PortalId enteredId, Vec3 enteredPos, PortalId exitedId, Vec3 exitedPos) {
		public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
				PortalId.STREAM_CODEC, Entry::enteredId,
				Vec3.STREAM_CODEC, Entry::enteredPos,
				PortalId.STREAM_CODEC, Entry::exitedId,
				Vec3.STREAM_CODEC, Entry::exitedPos,
				Entry::new
		);
	}
}
