package io.github.fusionflux.portalcubed.content.portal.sync;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.packet.serverbound.RequestEntitySyncPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class TeleportProgressTracker {
	/**
	 * Ticks until tracking gives up. Magic number, chosen because it's short but sufficient.
	 * Needs to be greater than the lerp steps for entities, which is 3 for everything as of 1.21.4.
	 * Also needs some spare time, since the entity is usually slightly farther behind.
	 */
	public static final int TIMEOUT_TICKS = 6;

	private final Entity entity;
	private final Deque<TrackedTeleport> teleports;

	public TeleportProgressTracker(Entity entity) {
		this.entity = entity;
		this.teleports = new LinkedList<>();
	}

	// called by the entity at the end of every tick
	public void tick() {
		if (this.teleports.isEmpty())
			return;

		for (TrackedTeleport teleport : this.teleports) {
			teleport.tick();
			if (teleport.hasTimedOut()) {
				// timeout, give up on all current tracking.
				this.abort();
				return;
			}
		}

		// iterate teleports in order, checking each one if it's done.
		// if it is, apply it's state, and continue to the next one.
		// this lets multiple teleports in the same tick all apply.
		Vec3 center = PortalTeleportHandler.centerOf(this.entity);
		Iterator<TrackedTeleport> itr = this.teleports.iterator();
		while (itr.hasNext()) {
			TrackedTeleport teleport = itr.next();
			Vec3 to = teleport.threshold.origin().vectorTo(center);
			double dot = to.dot(teleport.threshold.normal());
			if (teleport.isDone(center)) {
				itr.remove();
				System.out.println("teleport done; " + this.teleports);
				teleport.endState.apply(this.entity);
//				System.out.println("teleported to " + teleport.endState.pos());
			} else {
				break;
			}
		}
	}

	public void addTeleports(List<TrackedTeleport> teleports) {
		this.teleports.addAll(teleports);
	}

	public boolean isTracking() {
		return !this.teleports.isEmpty();
	}

	public TrackedTeleport currentTeleport() {
		return this.teleports.getFirst();
	}

	private void abort() {
		System.out.println("aborted tracking");
		this.teleports.clear();
		RequestEntitySyncPacket packet = new RequestEntitySyncPacket(this.entity);
//		PortalCubedPackets.sendToServer(packet);
	}
}
