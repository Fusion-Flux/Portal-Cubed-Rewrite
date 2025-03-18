package io.github.fusionflux.portalcubed.content.portal.sync;

import java.util.concurrent.atomic.AtomicInteger;

import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.shape.Plane;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public final class TrackedTeleport {
	public static final StreamCodec<ByteBuf, TrackedTeleport> CODEC = StreamCodec.composite(
			Plane.CODEC, teleport -> teleport.threshold,
			SinglePortalTransform.CODEC, teleport -> teleport.transform,
			ByteBufCodecs.VAR_INT, teleport -> teleport.id,
			TrackedTeleport::new
	);

	private static final AtomicInteger idGenerator = new AtomicInteger();

	public final Plane threshold;
	public final SinglePortalTransform transform;

	private final int id;

	private int ticksLeft = TeleportProgressTracker.TIMEOUT_TICKS;

	public TrackedTeleport(Plane threshold, SinglePortalTransform transform) {
		this(threshold, transform, idGenerator.getAndIncrement());
	}

	private TrackedTeleport(Plane threshold, SinglePortalTransform transform, int id) {
		this.threshold = threshold;
		this.transform = transform;
		this.id = id;
	}

	public void tick() {
		this.ticksLeft--;
	}

	public boolean isDone(Vec3 entityCenter) {
		return this.threshold.isBehind(entityCenter);
	}

	public boolean hasTimedOut() {
		return this.ticksLeft <= 0;
	}

	@Override
	public String toString() {
		return String.valueOf(this.id) + '(' + this.ticksLeft + ')';
	}
}
