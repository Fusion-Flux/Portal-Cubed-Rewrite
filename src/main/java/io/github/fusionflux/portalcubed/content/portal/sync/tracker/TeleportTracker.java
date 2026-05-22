package io.github.fusionflux.portalcubed.content.portal.sync.tracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.StringJoiner;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.sync.EntityState;
import io.github.fusionflux.portalcubed.content.portal.sync.TeleportStep;
import io.github.fusionflux.portalcubed.content.portal.sync.TrackedTeleport;
import io.github.fusionflux.portalcubed.content.portal.transform.MultiPortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/// Tracks a series of teleports that the remote has claimed occurred, expecting them to be followed through.
///
/// On the client, all entities have a tracker, except the [local player][LocalPlayer]. When an entity teleports
/// on the server, the client is notified, and that entity's tracker waits for the teleport to complete locally.
///
/// On the server, only [players][ServerPlayer] have teleport trackers. Clients tell the server when a teleport occurs,
/// and then the server waits for the teleport to be completed on the server as well.
public abstract sealed class TeleportTracker permits ClientTeleportTracker, ServerPlayerTeleportTracker {
	public static final boolean ENABLE_DEBUG_LOGGING = false;
	/// Ticks until tracking gives up. Magic number, chosen through trial and error.
	/// Needs to be greater than the lerp steps for entities, which is 3 for everything as of 1.21.4
	/// (except legacy minecarts, still adding +2).
	/// Also needs some spare time, since the entity is usually slightly farther behind.
	public static final int TIMEOUT_TICKS = 6;

	private static final Logger logger = LogUtils.getLogger();

	private final Queue<TrackedTeleport> teleports;
	private final List<SinglePortalTransform> reverseTransforms;

	/// List of teleport steps performed during a tick. Updated after each entity tick, for use in rendering.
	private final List<TeleportStep> currentSteps;

	protected TeleportTracker() {
		this.teleports = new LinkedList<>();
		this.reverseTransforms = new ArrayList<>();
		this.currentSteps = new LinkedList<>();
	}

	public void afterTick() {
		this.currentSteps.clear();

		if (this.teleports.isEmpty())
			return;

		if (ENABLE_DEBUG_LOGGING) {
			StringJoiner joiner = new StringJoiner(", ", "[", "]");
			this.teleports.forEach(teleport -> joiner.add(String.valueOf(teleport.ticksLeft())));
			logger.info("Teleports: {}", joiner);
		}

		for (TrackedTeleport teleport : this.teleports) {
			teleport.tick();
			if (teleport.hasTimedOut()) {
				// timeout, give up on all current tracking.
				this.abortAndSync();
				return;
			}
		}

		Entity entity = this.entity();

		// iterate teleports in order, checking each one if it's done.
		// if it is, apply its transform, and continue to the next one.
		// this lets multiple teleports in the same tick all apply.
		Iterator<TrackedTeleport> itr = this.teleports.iterator();
		int remainingTeleports = this.teleports.size();
		while (itr.hasNext()) {
			TrackedTeleport teleport = itr.next();
			// need to re-get center each time, since the entity moves after each teleport
			Vec3 center = PortalTeleportHandler.centerOf(entity);
			if (!teleport.isDone(center))
				break;

			if (ENABLE_DEBUG_LOGGING) {
				logger.info("Finished teleport: {}", teleport);
			}

			itr.remove();
			remainingTeleports--;
			this.reverseTransforms.removeLast();

			Vec3 oldCenter = PortalTeleportHandler.oldCenterOf(entity);
			Vec3 clip = teleport.threshold.clip(oldCenter, center);

			if (clip == null) {
				throw new IllegalStateException("Failed to find clip point of teleport");
			}

			double totalDistance = oldCenter.distanceTo(center);
			double distancePreTp = oldCenter.distanceTo(clip);
			float progressPreTp = (float) (distancePreTp / totalDistance);

			EntityState state = EntityState.capture(entity);
			EntityState old = EntityState.captureOld(entity);
			this.currentSteps.add(new TeleportStep(progressPreTp, old, state));

			teleport.transform.apply(entity);
			this.afterTeleport(teleport);

			EntityState afterTp = EntityState.capture(entity);
			EntityState oldAfterTp = EntityState.captureOld(entity);
			this.currentSteps.add(new TeleportStep(1f / remainingTeleports, oldAfterTp, afterTp));
		}
	}

	public void addTeleports(PortalPath.Serialized path) {
		PortalManager manager = this.entity().level().portalManager();

		switch (path.resolve(manager)) {
			case DataResult.Error<?> ignored -> {
				logger.warn("Failed to resolve portal path, aborting tracking");
				this.abortAndSync();
			}
			case DataResult.Success<PortalPath> success -> {
				for (PortalPath.Entry entry : success.value().entries()) {
					TrackedTeleport teleport = new TrackedTeleport(entry);
					this.teleports.add(teleport);
					this.reverseTransforms.add(teleport.transform.inverse());
				}
			}
		}
	}

	@Nullable
	public TrackedTeleport currentTeleport() {
		return this.teleports.peek();
	}

	/**
	 * Transform encompassing transforms of all teleports, inverted, in reverse order.
	 */
	@Nullable
	public PortalTransform reverseTransform() {
		return this.reverseTransforms.isEmpty() ? null : new MultiPortalTransform(this.reverseTransforms);
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

	public void abort() {
		this.teleports.clear();
		this.reverseTransforms.clear();
		this.currentSteps.clear();
	}

	protected void afterTeleport(TrackedTeleport teleport) {}

	protected abstract Entity entity();

	protected abstract void syncAfterAbort();

	private void abortAndSync() {
		this.abort();
		this.syncAfterAbort();
	}

	public static Optional<TeleportTracker> tryCreate(Entity entity) {
		if (entity instanceof ServerPlayer player) {
			return Optional.of(new ServerPlayerTeleportTracker(player));
		} else if (entity.level().isClientSide() && !isLocalPlayer(entity)) {
			return Optional.of(new ClientTeleportTracker(entity));
		} else {
			return Optional.empty();
		}
	}

	public static Optional<TeleportTracker> of(Entity entity) {
		return entity.pc$teleportTracker();
	}

	public static TeleportTracker getOrThrow(Entity entity) {
		return of(entity).orElseThrow(() -> new IllegalStateException("Entity " + entity + " does not have a TeleportTracker"));
	}

	private static boolean isLocalPlayer(Entity entity) {
		return entity instanceof Player player && player.isLocalPlayer();
	}
}
