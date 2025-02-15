package io.github.fusionflux.portalcubed.content.portal.sync;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
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

	public void afterTick() {
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
			DebugRendering.addPos(10, center, Color.YELLOW);
			if (teleport.isDone(center)) {
				itr.remove();
				System.out.println("teleport done; left: " + this.teleports.size());
				teleport.transform.apply(this.entity);
			} else {
				break;
			}
		}
	}

	public void addTeleports(List<TrackedTeleport> teleports) {
		this.teleports.addAll(teleports);
	}

	@Nullable
	public TrackedTeleport currentTeleport() {
		return this.teleports.peekFirst();
	}

	private void abort() {
		System.out.println("aborted tracking");
		this.teleports.clear();
		RequestEntitySyncPacket packet = new RequestEntitySyncPacket(this.entity);
		PortalCubedPackets.sendToServer(packet);
	}
}
