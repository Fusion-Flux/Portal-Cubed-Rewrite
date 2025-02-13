package io.github.fusionflux.portalcubed.content.portal.sync;

import io.github.fusionflux.portalcubed.content.portal.PortalTransform;
import io.github.fusionflux.portalcubed.framework.util.Plane;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public final class TrackedTeleport {
	public static final StreamCodec<ByteBuf, TrackedTeleport> CODEC = StreamCodec.composite(
			Plane.CODEC, teleport -> teleport.threshold,
			PortalTransform.CODEC, teleport -> teleport.transform,
			EntityState.CODEC, teleport -> teleport.endState,
			TrackedTeleport::new
	);

	public final Plane threshold;
	public final PortalTransform transform;
	public final EntityState endState;

	private int ticksLeft = TeleportProgressTracker.TIMEOUT_TICKS;

	public TrackedTeleport(Plane threshold, PortalTransform transform, EntityState endState) {
		this.threshold = threshold;
		this.transform = transform;
		this.endState = endState;
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
}
