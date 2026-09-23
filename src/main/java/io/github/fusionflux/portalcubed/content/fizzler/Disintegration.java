package io.github.fusionflux.portalcubed.content.fizzler;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedParticles;
import io.github.fusionflux.portalcubed.content.PortalCubedStats;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.DisintegratePacket;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/// Class managing several aspects of entity disintegration.
///
/// While an entity is disintegrating, it stops interacting with the world in most ways.
/// - They no longer [tick][Entity#tick()] (instead, [#tickDisintegrating(Entity)] is called)
/// - They are not [collided with][EntityGetter#getEntityCollisions(Entity, AABB)]
/// - They [ignore block triggers][Entity#isIgnoringBlockTriggers()] (tripwires, pressure plates, floor buttons)
/// - They are [silent][Entity#isSilent()]
/// - They do not have [gravity][Entity#isNoGravity()]
/// - They are [invulnerable][Entity#isInvulnerable()]
/// - They are not [alive][Entity#isAlive()]
/// - They cannot [be touched by players][Entity#playerTouch(Player)]
/// - They cannot be [picked up][ItemEntity#hasPickUpDelay()]
/// - They cannot [be leashed][Leashable#canHaveALeashAttachedTo]
/// - They cannot [hold or be held][HoldableEntity]
/// - [Many events][#registerEventListeners()] are cancelled while an involved entity is disintegrating
public final class Disintegration {
	/// The number of ticks it takes an entity to fully disintegrate.
	public static final int DISINTEGRATE_TICKS = 3 * 20;
	/// The number of ticks remaining when the entity starts to fade out.
	public static final int TRANSLUCENCY_START_TICKS = 10;

	/// Event invoked right before an entity starts disintegrating, on both the client and server.
	///
	/// On the client, this is invoked both when an entity actually starts disintegrating,
	/// and when an already disintegrating entity is loaded (ex. entering render distance).
	/// The difference can be detected by checking the remaining ticks.
	public static final Event<StartCallback> START_EVENT = EventFactory.createArrayBacked(StartCallback.class, callbacks -> (entity, ticks) -> {
		for (StartCallback callback : callbacks) {
			callback.onDisintegrationStart(entity, ticks);
		}
	});
	/// Event invoked each tick while an entity is disintegrating.
	public static final Event<Consumer<Entity>> TICK_EVENT = EventFactory.createArrayBacked(Consumer.class, callbacks -> entity -> {
		for (Consumer<Entity> callback : callbacks) {
			callback.accept(entity);
		}
	});

	// intentionally not auto-synced, done manually
	private static final AttachmentType<Integer> attachment = AttachmentRegistry.create(
			PortalCubed.id("disintegration_ticks"), builder -> builder.persistent(Codec.intRange(1, DISINTEGRATE_TICKS))
	);

	/// Try to start disintegrating the given entity.
	/// @return true if the entity started to disintegrate, false if it was already disintegrating
	/// @throws IllegalArgumentException if called with the client entity
	public static boolean disintegrate(Entity entity) {
		if (!(entity.level() instanceof ServerLevel)) {
			throw new IllegalArgumentException("Entities must be disintegrated from the server");
		}

		if (isDisintegrating(entity))
			return false;

		startDisintegrating(entity, DISINTEGRATE_TICKS);
		Collection<ServerPlayer> tracking = PortalCubedPackets.trackingAndMaybeSelf(entity);
		if (!tracking.isEmpty()) {
			PortalCubedPackets.sendToClients(tracking, new DisintegratePacket(entity));
		}

		return true;
	}

	/// @return true if the given entity is currently disintegrating
	public static boolean isDisintegrating(Entity entity) {
		return remainingDisintegrationTicks(entity) > 0;
	}

	/// @return the number of ticks left before an entity finishes disintegrating, or 0 if the entity is not disintegrating
	public static int remainingDisintegrationTicks(Entity entity) {
		Integer ticksLeft = entity.getAttached(attachment);
		return ticksLeft == null ? 0 : ticksLeft;
	}

	@ApiStatus.Internal
	public static void handleSync(Entity entity, int ticks) {
		// clamp it just in case...
		startDisintegrating(entity, Math.clamp(ticks, 1, DISINTEGRATE_TICKS));
	}

	@ApiStatus.Internal
	public static <T extends Mob> @Nullable T handleConversion(Mob from, ConversionParams params, Supplier<@Nullable T> to) {
		int ticks = remainingDisintegrationTicks(from);
		if (ticks <= 0) {
			return to.get();
		}

		// we need to treat the original entity as not disintegrating while converting, or else things get copied weirdly.
		// for example, a disintegrating slime will split into several gravity-less ones, since Entity#isNoGravity will be true.


		try {
			from.removeAttached(attachment);
			T converted = to.get();

			// when doing a single conversion, we want to carry over the disintegration.
			if (converted != null && params.type() == ConversionType.SINGLE) {
				converted.setAttached(attachment, ticks);
			}

			return converted;
		} finally {
			from.setAttached(attachment, ticks);
		}
	}

	/// While disintegrating, we cancel the original tick and run this instead.
	@ApiStatus.Internal
	public static void tickDisintegrating(Entity entity) {
		int ticks = remainingDisintegrationTicks(entity);
		if (ticks <= 0) {
			throw new IllegalArgumentException("Invalid disintegrationTicks: " + ticks);
		}

		if (entity.canSimulateMovement()) {
			Vec3 velocity = entity.getDeltaMovement().scale(.91);
			entity.move(MoverType.SELF, velocity);
			entity.setDeltaMovement(velocity);
		}

		// only emit particles before turning transparent
		if (ticks > TRANSLUCENCY_START_TICKS) {
			emitParticles(entity);
		}

		TICK_EVENT.invoker().accept(entity);

		if (ticks > 1) {
			// not done, decrement the timer
			entity.setAttached(attachment, ticks - 1);
			return;
		}

		// last tick, apply effects and clear the timer
		if (entity.level() instanceof ServerLevel serverLevel) {
			DisintegrateEffect.applyAll(serverLevel, entity);
		}

		// do this last so the entity still counts as disintegrating while applying effects
		entity.removeAttached(attachment);
	}

	private static void emitParticles(Entity entity) {
		Level level = entity.level();
		RandomSource random = entity.getRandom();
		Vec3 pos = entity.position();
		AABB bounds = entity.getBoundingBox();
		double width = bounds.getXsize();
		double height = bounds.getYsize();
		double length = bounds.getZsize();

		// P1 props don't make ash when fizzled
		if (!entity.is(PortalCubedEntityTags.FIZZLES_WITHOUT_DARK_PARTICLES)) {
			double volume = width * height * length;
			// maximum of 100/tick so that fizzling something large doesn't instantly kill performance.
			// minimum of 1/tick to prevent entities with small volumes (mug, item) from not making ash.
			for (int i = 0; i < Math.clamp(Math.round(volume * 15), 1, 100); i++) {
				double xOffset = random.nextGaussian() * (width / 3);
				double yOffset = .2 + (random.nextGaussian() * (height / 3));
				double zOffset = random.nextGaussian() * (length / 3);
				double velocityX = random.nextGaussian();
				double velocityY = random.nextGaussian();
				double velocityZ = random.nextGaussian();
				level.addParticle(PortalCubedParticles.FIZZLE_DARK, pos.x + xOffset, pos.y + yOffset, pos.z + zOffset, velocityX, velocityY, velocityZ);
			}
		}

		// Some props don't make the bright particles
		if (entity.is(PortalCubedEntityTags.FIZZLES_WITHOUT_BRIGHT_PARTICLES))
			return;

		// P1 props have an alternate bright particle type
		if (entity.is(PortalCubedEntityTags.FIZZLES_WITH_ALTERNATE_BRIGHT_PARTICLES)) {
			for (int i = 0; i < 3; i++) {
				double xOffset = random.nextGaussian() * (width / 3.8);
				double yOffset = .2 + (random.nextGaussian() * (height / 3.8));
				double zOffset = random.nextGaussian() * (length / 3.8);
				level.addParticle(PortalCubedParticles.FIZZLE_BRIGHT_ALTERNATE, pos.x + xOffset, pos.y + yOffset, pos.z + zOffset, 0, 0, 0);
			}
		} else {
			Vec3 center = bounds.getCenter();
			for (int i = 0; i < 3; i++) {
				level.addParticle(PortalCubedParticles.FIZZLE_BRIGHT, center.x, center.y, center.z, 0, 0, 0);
			}
		}
	}

	private static void startDisintegrating(Entity entity, int ticks) {
		START_EVENT.invoker().onDisintegrationStart(entity, ticks);
		entity.setAttached(attachment, ticks);
	}

	public static void registerEventListeners() {
		// we need to cancel a bunch of events when entities are disintegrating.
		// we do this in an early phase to prevent other mods from receiving the canceled events.
		Identifier phase = PortalCubed.id("disintegration");

		AttackBlockCallback.EVENT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
		AttackBlockCallback.EVENT.register(phase, (player, _, _, _, _) -> isDisintegrating(player) ? InteractionResult.FAIL : InteractionResult.PASS);

		AttackEntityCallback.EVENT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
		AttackEntityCallback.EVENT.register(phase, (player, _, _, entity, _) -> isDisintegrating(player) || isDisintegrating(entity) ? InteractionResult.FAIL : InteractionResult.PASS);

		PlayerBlockBreakEvents.BEFORE.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
		PlayerBlockBreakEvents.BEFORE.register(phase, (_, player, _, _, _) -> !isDisintegrating(player));

		UseBlockCallback.EVENT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
		UseBlockCallback.EVENT.register(phase, (player, _, _, _) -> isDisintegrating(player) ? InteractionResult.FAIL : InteractionResult.PASS);

		UseEntityCallback.EVENT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
		UseEntityCallback.EVENT.register(phase, (player, _, _, entity, _) -> isDisintegrating(player) || isDisintegrating(entity) ? InteractionResult.FAIL : InteractionResult.PASS);

		UseItemCallback.EVENT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
		UseItemCallback.EVENT.register(phase, (player, _, _) -> isDisintegrating(player) ? InteractionResult.FAIL : InteractionResult.PASS);

		START_EVENT.register((entity, _) -> {
			if (entity.level().isClientSide())
				return;

			entity.gameEvent(GameEvent.ENTITY_DIE);
			entity.stopRiding();

			if (entity instanceof LivingEntity living) {
				living.releaseUsingItem();
			}

			if (entity instanceof Leashable leashable) {
				leashable.dropLeash();
			}

			if (entity instanceof ServerPlayer player) {
				player.awardStat(PortalCubedStats.TIMES_DISINTEGRATED);
			}
		});
	}

	public static void registerClientEventListeners() {
		START_EVENT.register((entity, remainingTicks) -> {
			if (entity.level().isClientSide() && remainingTicks == DISINTEGRATE_TICKS) {
				// play sounds on the first tick of disintegration
 				DisintegrationSoundType.playAll(entity);
			}
		});

		// prevent opening screens when disintegrating (based on LocalPlayer#handlePortalTransitionEffect)
		TICK_EVENT.register(entity -> {
			if (entity instanceof Player player && player.isLocalPlayer()) {
				Gui gui = Minecraft.getInstance().gui;
				Screen screen = gui.screen();
				if (screen != null && !screen.isPauseScreen() && !(screen instanceof DeathScreen)) {
					if (screen instanceof AbstractContainerScreen) {
						((LocalPlayer) player).closeContainer();
					}

					gui.setScreen(null);
				}
			}
		});
	}

	@FunctionalInterface
	public interface StartCallback {
		/// @see #START_EVENT
		void onDisintegrationStart(Entity entity, int remainingTicks);
	}
}
