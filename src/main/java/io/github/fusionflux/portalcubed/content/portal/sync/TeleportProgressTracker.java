package io.github.fusionflux.portalcubed.content.portal.sync;

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
	 * Ticks until tracking gives up. Magic number, chosen through trial and error.
	 * Needs to be greater than the lerp steps for entities, which is 3 for everything as of 1.21.4
	 * (except legacy minecarts, still adding +2).
	 * Also needs some spare time, since the entity is usually slightly farther behind.
	 */
	public static final int TIMEOUT_TICKS = 5;

	private final Entity entity;
	private final LinkedList<TrackedTeleport> teleports;
	private final ReverseTeleportChain chain;

	public TeleportProgressTracker(Entity entity) {
		this.entity = entity;
		this.teleports = new LinkedList<>();
		this.chain = new ReverseTeleportChain(this.teleports);
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
		Iterator<TrackedTeleport> itr = this.teleports.iterator();
		while (itr.hasNext()) {
			// need to re-get center, since the entity moves after each teleport
			Vec3 center = PortalTeleportHandler.centerOf(this.entity);
			TrackedTeleport teleport = itr.next();
			Vec3 to = teleport.threshold.origin().vectorTo(center);
			double dot = to.dot(teleport.threshold.normal());
			DebugRendering.addPos(10, center, Color.YELLOW);
			if (teleport.isDone(center)) {
				itr.remove();
				System.out.println("teleport done; left: " + this.teleports);
				teleport.transform.apply(this.entity);
			} else {
				break;
			}
		}
	}

	public void addTeleports(List<TrackedTeleport> teleports) {
		this.teleports.addAll(teleports);
		System.out.println("added " + teleports.size() + " teleports; new: " + this.teleports);
	}

	@Nullable
	public TrackedTeleport currentTeleport() {
		return this.teleports.peekFirst();
	}

	public ReverseTeleportChain chain() {
		return this.chain;
	}

	private void abort() {
		System.out.println("aborted tracking");
		this.teleports.clear();
		RequestEntitySyncPacket packet = new RequestEntitySyncPacket(this.entity);
		PortalCubedPackets.sendToServer(packet);
	}
}
