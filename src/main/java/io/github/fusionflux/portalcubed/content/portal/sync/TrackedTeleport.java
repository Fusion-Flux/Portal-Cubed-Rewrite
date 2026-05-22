package io.github.fusionflux.portalcubed.content.portal.sync;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.sync.tracker.TeleportTracker;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.shape.Plane;
import net.minecraft.world.phys.Vec3;

/// A teleport that has occurred on one side, and is expected to also occur on the other one soon.
public final class TrackedTeleport {
	public final PortalPath.Entry pathEntry;
	public final Plane threshold;
	public final SinglePortalTransform transform;

	private int ticksLeft;

	public TrackedTeleport(PortalPath.Entry entry) {
		this.pathEntry = entry;
		this.threshold = entry.entered().reference().get().plane;
		this.transform = entry.createTransform();
		this.ticksLeft = TeleportTracker.TIMEOUT_TICKS;
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

	public int ticksLeft() {
		return this.ticksLeft;
	}

	@Override
	public String toString() {
		return "TrackedTeleport[%d, %s -> %s]".formatted(
				this.ticksLeft,
				this.pathEntry.entered().reference().id,
				this.pathEntry.exited().reference().id
		);
	}
}
