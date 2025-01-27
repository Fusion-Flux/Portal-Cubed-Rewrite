package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

public interface DisintegrationExt {
	int DISINTEGRATE_TICKS = 3 * 20;
	int TRANSLUCENCY_START_TICKS = 10;
	ResourceLocation DISINTEGRATION_INTERACTION_PHASE = PortalCubed.id("disintegration");

	static void registerEventListeners() {
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
		UseItemCallback.EVENT.register(DISINTEGRATION_INTERACTION_PHASE, (player, world, hand) -> player.pc$disintegrating() ? InteractionResult.FAIL : InteractionResult.PASS);
	}

	default boolean pc$disintegrate() {
		throw new AbstractMethodError();
	}

	default void pc$disintegrate(int ticks) {
		throw new AbstractMethodError();
	}

	default boolean pc$disintegrating() {
		throw new AbstractMethodError();
	}

	default void pc$disintegrating(boolean disintegrating) {
		throw new AbstractMethodError();
	}

	default int pc$disintegrateTicks() {
		throw new AbstractMethodError();
	}

	default void pc$disintegrateTick() {
		throw new AbstractMethodError();
	}
}
