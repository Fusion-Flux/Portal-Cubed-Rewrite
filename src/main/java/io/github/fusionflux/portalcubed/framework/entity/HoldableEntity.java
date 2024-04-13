package io.github.fusionflux.portalcubed.framework.entity;

import java.util.OptionalInt;

import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;

import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.HoldStatusPacket;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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
	private static final EntityDataAccessor<OptionalInt> HOLDER = SynchedEntityData.defineId(HoldableEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);

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
		Vec3 holdPoint = holder.getEyePosition()
				.add(Vec3.directionFromRotation(holder.getXRot(), holder.getYRot()).scale(2))
				.add(0, -getBbHeight() / 2, 0);
		Vec3 toPoint = this.position().vectorTo(holdPoint);
//		this.setDeltaMovement(toPoint.scale(1 / 20f));
		this.move(MoverType.SELF, toPoint);

		// rotate to face player, unless taco
		if (this.facesHolder()) {
			this.setYRot((holder.getYRot() + 180) % 360);
		}

		// drop when too far away
		if (!this.level().isClientSide && position().distanceToSqr(holder.getEyePosition()) >= Mth.square(4.5))
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

	public void grab(ServerPlayer player) {
		if (this.holder != null) {
			// check if the new player can steal it
			if (!player.serverLevel().getGameRules().getBoolean(PortalCubedGameRules.PROP_SNATCHING))
				return; // can't, don't do anything

			// can, drop first, then re-grab
			this.drop();
		}

		this.entityData.set(HOLDER, OptionalInt.of(player.getId()));
		((PlayerExt) player).pc$setHeldProp(OptionalInt.of(this.getId()));
		updateHoldStatus(player, this);
	}

	public void drop() {
		// implicit null check
		if (this.holder instanceof ServerPlayer player) {
			((PlayerExt) player).pc$setHeldProp(OptionalInt.empty());
			this.entityData.set(HOLDER, OptionalInt.empty());
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
			if (tracked instanceof PlayerExt otherPlayer) {
				OptionalInt held = otherPlayer.pc$getHeldProp();
				if (held.isPresent()) {
					PortalCubedPackets.sendToClient(player, new HoldStatusPacket(tracked.getId(), held));
				}
			}
		});
	}
}
