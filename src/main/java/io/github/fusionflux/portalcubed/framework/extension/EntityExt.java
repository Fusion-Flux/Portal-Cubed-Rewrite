package io.github.fusionflux.portalcubed.framework.extension;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.TeleportProgressTracker;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.DisintegratePacket;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

public interface EntityExt {
	int DISINTEGRATE_TICKS = 3 * 20;
	int TRANSLUCENCY_START_TICKS = 10;
	ResourceLocation DISINTEGRATION_INTERACTION_PHASE = PortalCubed.id("disintegration");

	static void registerEventListeners() {
		EntityTrackingEvents.AFTER_START_TRACKING.register((tracked, player) -> {
			if (tracked.pc$disintegrating()) PortalCubedPackets.sendToClient(player, new DisintegratePacket(tracked));
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (handler.player.pc$disintegrating()) sender.sendPayload(new DisintegratePacket(handler.player));
		});

		AttackBlockCallback.EVENT.addPhaseOrdering(DISINTEGRATION_INTERACTION_PHASE, Event.DEFAULT_PHASE);
		AttackBlockCallback.EVENT.register(DISINTEGRATION_INTERACTION_PHASE, (player, world, hand, pos, direction) -> player.pc$disintegrating() ? InteractionResult.FAIL : InteractionResult.PASS);
		AttackEntityCallback.EVENT.addPhaseOrdering(DISINTEGRATION_INTERACTION_PHASE, Event.DEFAULT_PHASE);
		AttackEntityCallback.EVENT.register(DISINTEGRATION_INTERACTION_PHASE, (player, world, hand, entity, hitResult) ->
				(player.pc$disintegrating() || entity.pc$disintegrating()) ? InteractionResult.FAIL : InteractionResult.PASS);
		PlayerBlockBreakEvents.BEFORE.addPhaseOrdering(DISINTEGRATION_INTERACTION_PHASE, Event.DEFAULT_PHASE);
		PlayerBlockBreakEvents.BEFORE.register(DISINTEGRATION_INTERACTION_PHASE, (world, player, pos, state, blockEntity) -> !player.pc$disintegrating());
		UseBlockCallback.EVENT.addPhaseOrdering(DISINTEGRATION_INTERACTION_PHASE, Event.DEFAULT_PHASE);
		UseBlockCallback.EVENT.register(DISINTEGRATION_INTERACTION_PHASE, (player, world, hand, hitResult) -> player.pc$disintegrating() ? InteractionResult.FAIL : InteractionResult.PASS);
		UseEntityCallback.EVENT.addPhaseOrdering(DISINTEGRATION_INTERACTION_PHASE, Event.DEFAULT_PHASE);
		UseEntityCallback.EVENT.register(DISINTEGRATION_INTERACTION_PHASE, (player, world, hand, entity, hitResult) ->
				(player.pc$disintegrating() || entity.pc$disintegrating()) ? InteractionResult.FAIL : InteractionResult.PASS);
		UseItemCallback.EVENT.addPhaseOrdering(DISINTEGRATION_INTERACTION_PHASE, Event.DEFAULT_PHASE);
		UseItemCallback.EVENT.register(DISINTEGRATION_INTERACTION_PHASE, (player, world, hand) -> player.pc$disintegrating() ? InteractionResultHolder.fail(player.getItemInHand(hand)) : InteractionResultHolder.pass(player.getItemInHand(hand)));
	}

	default boolean pc$disintegrate() {
		throw new AbstractMethodError();
	}

	default boolean pc$disintegrate(int ticks) {
		throw new AbstractMethodError();
	}

	default boolean pc$disintegrating() {
		throw new AbstractMethodError();
	}

	default int pc$disintegrateTicks() {
		throw new AbstractMethodError();
	}

	default void pc$disintegrateTick() {
		throw new AbstractMethodError();
	}

	default int pc$getPortalCollisionRecursionDepth() {
		throw new AbstractMethodError();
	}

	default void pc$setPortalCollisionRecursionDepth(int depth) {
		throw new AbstractMethodError();
	}

	default void pc$setNextTeleportNonLocal(boolean value) {
		throw new AbstractMethodError();
	}

	default boolean pc$isNextTeleportNonLocal() {
		throw new AbstractMethodError();
	}

	// no prefix needed, unique descriptors

	@Nullable
	default TeleportProgressTracker getTeleportProgressTracker() {
		throw new AbstractMethodError();
	}

	default void setTeleportProgressTracker(@Nullable TeleportProgressTracker tracker) {
		throw new AbstractMethodError();
	}
}
