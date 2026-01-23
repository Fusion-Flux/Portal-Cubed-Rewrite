package io.github.fusionflux.portalcubed.content.portal.sync;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.transform.MultiPortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
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
	public static final int TIMEOUT_TICKS = 6;

	private final Entity entity;
	private final LinkedList<TrackedTeleport> teleports;
	private final List<SinglePortalTransform> reverseTransforms;
	private final MultiPortalTransform reverseTransform;

	private final List<TeleportStep> currentSteps;

	public TeleportProgressTracker(Entity entity) {
		this.entity = entity;
		this.teleports = new LinkedList<>();
		this.reverseTransforms = new ArrayList<>();
		this.reverseTransform = new MultiPortalTransform(this.reverseTransforms);
		this.currentSteps = new LinkedList<>();
	}

	public void afterTick() {
		this.currentSteps.clear();

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
		// if it is, apply its transform, and continue to the next one.
		// this lets multiple teleports in the same tick all apply.
		Iterator<TrackedTeleport> itr = this.teleports.iterator();
		int remainingTeleports = this.teleports.size();
		while (itr.hasNext()) {
			TrackedTeleport teleport = itr.next();
			// need to re-get center each time, since the entity moves after each teleport
			Vec3 center = PortalTeleportHandler.centerOf(this.entity);
			if (!teleport.isDone(center))
				break;

			itr.remove();
			remainingTeleports--;
			this.reverseTransforms.removeLast();

			Vec3 oldCenter = PortalTeleportHandler.oldCenterOf(this.entity);
			Vec3 clip = teleport.threshold.clip(oldCenter, center);
			// this shouldn't happen, but just in case
			if (clip == null)
				continue;

			double totalDistance = oldCenter.distanceTo(center);
			double distancePreTp = oldCenter.distanceTo(clip);
			float progressPreTp = (float) (distancePreTp / totalDistance);

			EntityState state = EntityState.capture(this.entity);
			EntityState old = EntityState.captureOld(this.entity);
			this.currentSteps.add(new TeleportStep(progressPreTp, old, state));

			teleport.transform.apply(this.entity);

			EntityState afterTp = EntityState.capture(this.entity);
			EntityState oldAfterTp = EntityState.captureOld(this.entity);
			this.currentSteps.add(new TeleportStep(1f / remainingTeleports, oldAfterTp, afterTp));
		}
	}

	public void addTeleports(List<TrackedTeleport> teleports) {
		for (TrackedTeleport teleport : teleports) {
			this.teleports.add(teleport);
			this.reverseTransforms.addFirst(teleport.transform.inverse());
		}
	}

	@Nullable
	public TrackedTeleport currentTeleport() {
		return this.teleports.peekFirst();
	}

	/**
	 * Transform encompassing transforms of all teleports, inverted, in reverse order.
	 */
	public PortalTransform reverseTransform() {
		return this.reverseTransform;
	}

	@Nullable
	public EntityState getEntityStateOverride(float partialTick) {
		for (TeleportStep step : this.currentSteps) {
			if (partialTick < step.weight()) {
				return step.getState(partialTick);
			}
		}
		return null;
	}

	private void abort() {
		this.teleports.clear();
		this.reverseTransforms.clear();
		RequestEntitySyncPacket packet = new RequestEntitySyncPacket(this.entity);
		PortalCubedPackets.sendToServer(packet);
	}
}
