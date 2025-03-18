package io.github.fusionflux.portalcubed.content.portal;

import java.util.ArrayList;
import java.util.List;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.sync.TrackedTeleport;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.shape.voxel.VoxelShenanigans;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.EntityAccessor;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.PortalTeleportPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.ClientTeleportedPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PortalTeleportHandler {
	public static final double MIN_OUTPUT_VELOCITY = 0.25;
	public static final double DISTANCE_TO_STEP_BACK = new Vec3(
			1f / VoxelShenanigans.OBB_APPROXIMATION_RESOLUTION,
			1f / VoxelShenanigans.OBB_APPROXIMATION_RESOLUTION,
			1f / VoxelShenanigans.OBB_APPROXIMATION_RESOLUTION
	).length();

	/**
	 * Called by mixins after an entity moves relatively.
	 * Responsible for finding and teleporting through portals.
	 */
	public static boolean handle(Entity entity, Vec3 oldPos) {
		if (cannotTeleport(entity))
			return false;

		Vec3 newPos = entity.position();
		Vec3 newCenter = centerOf(entity);
		Vec3 posToCenter = newPos.vectorTo(newCenter);
		Vec3 oldCenter = oldPos.add(posToCenter);

		PortalManager manager = entity.level().portalManager();
		PortalHitResult result = manager.activePortals().clip(oldCenter, newCenter);
		if (result == null)
			return false;

		if (theHorrors(result))
			return false;

		PortalTransform transform = PortalTransform.of(result);
		transform.apply(entity);

		// tp command does this
		if (entity instanceof PathfinderMob pathfinderMob) {
			pathfinderMob.getNavigation().stop();
		}

		// syncing stuff
		if (entity instanceof Player player && player.isLocalPlayer()) {
			// players are handled specially. All the logic is client side and the server is notified.
			// server does some verification and tells the client if the teleport was invalid.
			PortalTeleportInfo info = buildTeleportInfo(result);
			ClientTeleportedPacket packet = new ClientTeleportedPacket(info, entity.position(), entity.getXRot(), entity.getYRot());
			PortalCubedPackets.sendToServer(packet);
			return true;
		}

		if (!entity.level().isClientSide) {
			// sync to clients
			PortalTeleportPacket packet = new PortalTeleportPacket(entity.getId(), buildTeleports(result));
			PortalCubedPackets.sendToClients(PlayerLookup.tracking(entity), packet);
		}

		return true;
	}

	public static Vec3 centerOf(Entity entity) {
		return entity.getBoundingBox().getCenter();
	}

	public static Vec3 oldCenterOf(Entity entity) {
		Vec3 centerNow = centerOf(entity);
		Vec3 posToCenter = entity.position().vectorTo(centerNow);
		return entity.oldPosition().add(posToCenter);
	}

	private static boolean theHorrors(PortalHitResult result) {
		if (result.getLast() == PortalHitResult.OVERFLOW_MARKER) {
			// skip to last 10, toString can overflow
			while (result.hasNext()) {
				result = result.next();
				if (result.depth() == 10) {
					System.out.println(result);
				}
			}
			return true;
		}
		return false;
	}

	public static void nudge(Entity entity, Vec3 exitOrigin) {
		// because of the difference in a portal's plane and its collision, teleporting will always put an entity
		// either in the ground or floating slightly, depending on direction. Need to nudge the entity towards the
		// center and then back to the intended pos, stopping early if collision is hit.
		Vec3 center = centerOf(entity);
		Vec3 stepBack = center.vectorTo(exitOrigin).normalize().scale(DISTANCE_TO_STEP_BACK);
		entity.setPos(entity.position().add(stepBack));
		Vec3 stepForwards = stepBack.scale(-1);
		Vec3 completedStep = ((EntityAccessor) entity).callCollide(stepForwards);
		entity.setPos(entity.position().add(completedStep));
	}

	private static PortalTeleportInfo buildTeleportInfo(PortalHitResult result) {
		return new PortalTeleportInfo(
				result.pairKey(),
				result.pair().polarityOf(result.in()),
				result.hasNext() ? buildTeleportInfo(result.next()) : null
		);
	}

	private static List<TrackedTeleport> buildTeleports(PortalHitResult result) {
		List<TrackedTeleport> teleports = new ArrayList<>();

		while (result != null) {
			SinglePortalTransform transform = new SinglePortalTransform(result.in(), result.out());
			teleports.add(new TrackedTeleport(result.in().plane, transform));
			result = result.nextOrNull();
		}

		return teleports;
	}

	public static boolean cannotTeleport(Entity entity) {
		if (ignoresPortalModifiedCollision(entity))
			return true;

		// player teleportation is handled client-side
		if (entity instanceof Player player)
			return !player.isLocalPlayer();

		// all other entities teleport server-side
		return entity.level().isClientSide;
	}

	public static boolean ignoresPortalModifiedCollision(Entity entity) {
		return entity.isPassenger() || entity.isVehicle() || entity.getType().is(PortalCubedEntityTags.PORTAL_BLACKLIST);
	}

	// teleportation utilities

	public static OBB teleportBox(OBB box, PortalInstance in, PortalInstance out) {
		return box.transformed(
				center -> teleportAbsoluteVecBetween(center, in, out),
				rotation -> rotation
						.rotateLocal(in.rotation())
						.rotateLocal(out.rotation180)
		);
	}

	public static Vec3 teleportAbsoluteVecBetween(Vec3 vec, PortalInstance in, PortalInstance out) {
		return new SinglePortalTransform(in, out).applyAbsolute(vec);
	}

	public static Vec3 teleportRelativeVecBetween(Vec3 vec, PortalInstance in, PortalInstance out) {
		return new SinglePortalTransform(in, out).applyRelative(vec);
	}
}
