package io.github.fusionflux.portalcubed.framework.entity;

import java.util.OptionalInt;

import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;

import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.HoldStatusPacket;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.quiltmc.qsl.networking.api.EntityTrackingEvents;

// hold state
// - client keybind triggers sending a GrabPacket, calls grab(player)
// - server sets ID field on player, updates tracked data
// - tracked data update propagates to client
// - all players (including holder) are sent a HoldStatusPacket
// - HoldStatusPacket sets field on client players
// - client keybind sends DropPacket, calls drop()
// - field is set, tracked data updated
// - HoldStatusPackets sent, clients update
// - For existing holds, additional HoldStatusPackets are sent in AFTER_START_TRACKING.
public abstract class HoldableEntity extends LerpableEntity {
	public static final EntityDataAccessor<OptionalInt> HOLDER = SynchedEntityData.defineId(HoldableEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
	public static final double MAX_DIST_SQR = 3 * 3;

	@Nullable
	private Player holder;

	public HoldableEntity(EntityType<?> variant, Level world) {
		super(variant, world);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(HOLDER, OptionalInt.empty());
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		super.onSyncedDataUpdated(data);
		if (HOLDER.equals(data)) {
			OptionalInt id = this.entityData.get(HOLDER);
			if (id.isEmpty()) {
				this.holder = null;
			} else {
				Entity entity = this.level().getEntity(id.getAsInt());
				if (entity instanceof Player player) {
					this.holder = player;
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		Player holder = this.getHolder();
		if (holder == null)
			return;

		// move in front of player
		Vec3 holdPoint = holder.getLookAngle().scale(2)
				.add(holder.getEyePosition())
				.subtract(0, this.getBbHeight() / 2, 0);
		Vec3 toPoint = this.position().vectorTo(holdPoint);
		if (toPoint.length() > MAX_SPEED_SQR)
			toPoint = toPoint.normalize().scale(MAX_SPEED_SQR);
		this.setDeltaMovement(toPoint);
		this.move(MoverType.PLAYER, this.getDeltaMovement());

		// rotate to face player
		if (this.facesHolder()) {
			this.setYRot((holder.getYRot() + 180) % 360);
		}

		// drop when holder changes to spectator or moves too far away
		if (!this.level().isClientSide && (holder.isSpectator() || this.position().distanceToSqr(holder.getEyePosition()) >= MAX_DIST_SQR))
			this.drop();
	}

	@Override
	public boolean canCollideWith(Entity other) {
		if (other == this.holder) {
			return false;
		}
		return super.canCollideWith(other);
	}

	protected boolean facesHolder() {
		return true;
	}

	@Nullable
	public Player getHolder() {
		return holder;
	}

	public boolean isHeld() {
		return this.holder != null;
	}

	public boolean isHeldBy(Entity entity) {
		return this.holder == entity;
	}

	public boolean notHeldBy(Entity entity) {
		return !this.isHeldBy(entity);
	}

	public void grab(ServerPlayer player) {
		if (this.hasPassenger(player))
			return; // don't allow holding self

		if (this.holder != null) {
			// check if the new player can steal it
			if (!player.serverLevel().getGameRules().getBoolean(PortalCubedGameRules.PROP_SNATCHING))
				return; // can't, don't do anything

			// can, drop first, then re-grab
			this.drop();
		}

		this.entityData.set(HOLDER, OptionalInt.of(player.getId()));
		player.setHeldEntity(this);
		updateHoldStatus(player, this);
	}

	public void drop() {
		// implicit null check
		if (this.holder instanceof ServerPlayer player) {
			this.entityData.set(HOLDER, OptionalInt.empty());
			player.setHeldEntity(null);
			updateHoldStatus(player, null);
		}
	}

	private static void updateHoldStatus(ServerPlayer holder, @Nullable HoldableEntity held) {
		HoldStatusPacket packet = new HoldStatusPacket(holder, held);
		for (ServerPlayer toUpdate : PortalCubedPackets.trackingAndSelf(holder)) {
			PortalCubedPackets.sendToClient(toUpdate, packet);
		}
	}

	public static void registerEventListeners() {
		EntityTrackingEvents.AFTER_START_TRACKING.register((tracked, player) -> {
			if (tracked instanceof ServerPlayer otherPlayer) {
				HoldableEntity held = otherPlayer.getHeldEntity();
				if (held != null) {
					PortalCubedPackets.sendToClient(player, new HoldStatusPacket(otherPlayer, held));
				}
			}
		});
	}
}
