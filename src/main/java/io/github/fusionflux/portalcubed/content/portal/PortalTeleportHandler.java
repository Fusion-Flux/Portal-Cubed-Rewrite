package io.github.fusionflux.portalcubed.content.portal;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedCriteriaTriggers;
import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.sync.TrackedTeleport;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.PortalTeleportPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.ClientTeleportedPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PortalTeleportHandler {
	public static final double MIN_OUTPUT_VELOCITY = 0.25;

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
		PortalHitResult result = manager.lookup().clip(oldCenter, newCenter);
		if (result == null)
			return false;

		PortalTransform transform = PortalTransform.of(result);
		transform.apply(entity);

		if (entity instanceof ItemEntity item && item.getOwner() instanceof ServerPlayer player) {
			ItemStack stack = item.getItem();

			result.forEach(hit -> {
				PortalCubedCriteriaTriggers.THROWN_ITEM_ENTERED_PORTAL.trigger(player, hit.enteredPortal(), stack);
				PortalCubedCriteriaTriggers.THROWN_ITEM_EXITED_PORTAL.trigger(player, hit.exitedPortal(), stack);
			});
		}

		// wakey wakey
		if (entity instanceof LivingEntity living && living.isSleeping()) {
			try {
				living.pc$skipWakeUpMovement(true);
				living.stopSleeping();
			} finally {
				living.pc$skipWakeUpMovement(false);
			}
		}

		// tp command does this
		if (entity instanceof PathfinderMob pathfinderMob) {
			pathfinderMob.getNavigation().stop();
		}

		// syncing stuff
		if (entity instanceof Player player && player.isLocalPlayer()) {
			// players are handled specially. All the logic is client side and the server is notified.
			// server does some verification and tells the client if the teleport was invalid.
			PortalCubedPackets.sendToServer(ClientTeleportedPacket.of(player, result));
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

	private static List<TrackedTeleport> buildTeleports(PortalHitResult result) {
		List<TrackedTeleport> teleports = new ArrayList<>();

		result.forEach(open -> {
			SinglePortalTransform transform = new SinglePortalTransform(open);
			teleports.add(new TrackedTeleport(open.enteredPortal().get().plane, transform));
		});

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

	public static boolean ignoresPortalModifiedCollision(@Nullable Entity entity) {
		return entity == null || entity.isPassenger() || entity.isVehicle() || entity.getType().is(PortalCubedEntityTags.PORTAL_BLACKLIST);
	}
}
