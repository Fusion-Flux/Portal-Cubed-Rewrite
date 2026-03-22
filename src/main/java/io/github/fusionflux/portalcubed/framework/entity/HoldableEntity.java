package io.github.fusionflux.portalcubed.framework.entity;

import java.util.List;
import java.util.OptionalInt;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.HoldStatusPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
	public static final double HOLD_DISTANCE = 2;
	public static final EntityDataAccessor<OptionalInt> HOLDER = SynchedEntityData.defineId(HoldableEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);

	@Nullable
	private Player holder;

	protected HoldableEntity(EntityType<?> variant, Level world) {
		super(variant, world);
	}

	@Override
	protected void defineSynchedData(Builder builder) {
		builder.define(HOLDER, OptionalInt.empty());
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
	public boolean isEffectiveAi() {
		if (super.isEffectiveAi())
			return true;

		Player holder = this.getHolder();
		return holder != null && holder.isLocalPlayer();
	}

	@Override
	public void tick() {
		super.tick();
		Player holder = this.getHolder();
		if (holder == null)
			return;

		// move in front of player
		Vec3 trueTarget = this.determineMotionTarget(holder);
		if (trueTarget.distanceTo(this.position()) > 5) {
			// too far away, give up
			if (!this.level().isClientSide()) {
				this.drop();
			}

			return;
		}

		Vec3 motion = this.position().vectorTo(trueTarget);
		this.setDeltaMovement(motion);
		this.move(MoverType.PLAYER, this.getDeltaMovement());

		if (motion.y == 0) {
			// reset fall distance to not accumulate it while held
			this.resetFallDistance();
		}

		// rotate to face player
		if (this.facesHolder()) {
			this.setYRot((holder.getYRot() + 180) % 360);
		}

		// drop when holder is no longer valid or when we are no longer able to be held
		if (!this.level().isClientSide() && !this.canHold(holder)) {
			this.drop();
		}
	}

	private Vec3 determineMotionTarget(Player holder) {
		Vec3 eyePos = holder.getEyePosition();
		Vec3 lookVec = holder.getLookAngle();
		Vec3 target = eyePos.add(lookVec.scale(HOLD_DISTANCE));

		RaycastOptions options = RaycastOptions.DEFAULT.edit()
				.entities(EntitySelector.NO_SPECTATORS.and(this::canCollideWith))
				.collisionContext(this)
				.build();

		// this raycast is really just used to figure out which portals are relevant for the grab.
		// we give it some extra distance to account for portals that are barely too far away.
		// for example, walking backwards through a portal while holding something would drop it
		// right before it crosses the threshold, since the hold point would now be on the other side.
		RaycastResult result = options.raycast(this.level(), eyePos, lookVec, HOLD_DISTANCE * 2);
		Iterable<PortalPath.Entry> entries = result.path.map(PortalPath::entries).orElse(List.of());

		// this isn't correct, but it should generally be good enough.
		// find the relative hold point that is closest.
		Vec3 center = PortalTeleportHandler.centerOf(this);
		Vec3 closestTarget = target;
		Vec3 currentTarget = target;
		for (PortalPath.Entry entry : entries) {
			currentTarget = entry.createTransform().applyAbsolute(currentTarget);
			if (center.distanceToSqr(currentTarget) < center.distanceToSqr(closestTarget)) {
				closestTarget = currentTarget;
			}
		}

		return PortalTeleportHandler.uncenter(this, closestTarget);
	}

	@Override
	public boolean canCollideWith(Entity other) {
		if (other == this.holder) {
			return false;
		}
		return super.canCollideWith(other);
	}

	@Override
	public boolean pc$disintegrate() {
		if (!this.level().isClientSide)
			this.drop();
		return super.pc$disintegrate();
	}

	protected boolean facesHolder() {
		return true;
	}

	@Nullable
	public Player getHolder() {
		return this.holder;
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

	public boolean canHold(Player player) {
		return (!this.pc$disintegrating() && !this.isPassenger() && !this.hasPassenger(player)) // Self checks
				&& (!player.isSpectator() && player.canInteractWithEntity(this, 0)); // Holder checks
	}

	public void grab(ServerPlayer player) {
		if (!this.canHold(player))
			return;

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

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		if (this.holder instanceof ServerPlayer holder) {
			PortalCubedPackets.sendToClient(player, new HoldStatusPacket(holder, this));
		}
	}
}
