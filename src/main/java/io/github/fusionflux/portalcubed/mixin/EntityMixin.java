package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;

import io.github.fusionflux.portalcubed.framework.extension.EntityExt;

import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.DisintegratePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.server.level.ServerPlayer;

import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.framework.extension.AmbientSoundEmitter;
import io.github.fusionflux.portalcubed.framework.extension.CollisionListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExt {
	@Shadow
	@Final
	private static EntityDataAccessor<Boolean> DATA_SILENT;
	@Shadow
	public boolean horizontalCollision, verticalCollision, verticalCollisionBelow;

	@Shadow
	public abstract Level level();
	@Shadow
	public abstract SynchedEntityData getEntityData();

	@Shadow
	public abstract void discard();

	@Unique
	private boolean isHorizontalColliding, isTopColliding, isBelowColliding;
	@Unique
	private int disintegrateTicks;

	@Override
	public boolean pc$disintegrate() {
		boolean notDisintegrating = !this.pc$disintegrating();
		if (notDisintegrating && this.level() instanceof ServerLevel && (Object) this instanceof Entity self) {
			this.disintegrateTicks = DISINTEGRATE_TICKS;
			DisintegratePacket packet = new DisintegratePacket(self);
			for (ServerPlayer toUpdate : PlayerLookup.tracking(self)) {
				PortalCubedPackets.sendToClient(toUpdate, packet);
			}
		}
		return notDisintegrating;
	}

	@Override
	public boolean pc$disintegrate(int ticks) {
		if (this.pc$disintegrate()) {
			this.disintegrateTicks = ticks;
			return true;
		}
		return false;
	}

	@Override
	public boolean pc$disintegrating() {
		return this.disintegrateTicks != 0;
	}

	@Override
	public int pc$disintegrateTicks() {
		return this.disintegrateTicks;
	}

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
	private void readDisintegrateTicks(CompoundTag tag, CallbackInfo ci) {
		this.disintegrateTicks = tag.getInt("portalcubed:disintegrate_ticks");
	}

	@Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
	private void saveDisintegrateTicks(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (this.pc$disintegrating())
			tag.putInt("portalcubed:disintegrate_ticks", this.disintegrateTicks);
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void disintegrationTicking(CallbackInfo ci) {
		if (this.pc$disintegrating()) {
			if (--this.disintegrateTicks <= 0 && !this.level().isClientSide) {
				this.discard();
			}
			ci.cancel();
		}
	}

	@Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("RETURN"))
	private void startSoundWhenUnSilenced(EntityDataAccessor<?> data, CallbackInfo ci) {
		if (level().isClientSide && (Object) this instanceof AmbientSoundEmitter ambientSoundEmitter) {
			if (DATA_SILENT.equals(data) && !getEntityData().get(DATA_SILENT))
				ambientSoundEmitter.playAmbientSound();
		}
	}

	@Inject(
		method = "move",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
			shift = At.Shift.BEFORE
		)
	)
	private void listenForCollisions(MoverType movementType, Vec3 movement, CallbackInfo ci) {
		if (this instanceof CollisionListener collisionListener) {
			if (horizontalCollision) {
				if (!isHorizontalColliding) collisionListener.onCollision();
				isHorizontalColliding = true;
			} else {
				isHorizontalColliding = false;
			}

			if (verticalCollision) {
				if (!isTopColliding) collisionListener.onCollision();
				isTopColliding = true;
			} else {
				isTopColliding = false;
			}

			if (verticalCollisionBelow) {
				if (!isBelowColliding) collisionListener.onCollision();
				isBelowColliding = true;
			} else {
				isBelowColliding = false;
			}
		}
	}

	@Inject(method = "canCollideWith", at = @At("RETURN"), cancellable = true)
	private void dontCollideWithHeldProp(Entity other, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Player self && self.getHeldEntity() == other) {
			cir.setReturnValue(false);
		} else if (this.pc$disintegrating() || other.pc$disintegrating()) {
			cir.setReturnValue(false);
		}
	}

	@SuppressWarnings({"ConstantValue", "UnreachableCode"})
	@Inject(method = "setRemoved", at = @At("HEAD"))
	private void dropHeldWhenRemoved(RemovalReason reason, CallbackInfo ci) {
		if (this.level().isClientSide)
			return;

		if ((Object) this instanceof Player player && player.getHeldEntity() != null) {
			player.getHeldEntity().drop();
		} else if ((Object) this instanceof HoldableEntity holdable && holdable.isHeld()) {
			holdable.drop();
		}
	}
}
