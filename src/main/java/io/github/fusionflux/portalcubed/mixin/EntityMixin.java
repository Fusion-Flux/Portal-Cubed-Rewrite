package io.github.fusionflux.portalcubed.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.TeleportProgressTracker;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.framework.extension.EntityExt;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.RequestEntitySyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExt {
	@Shadow
	@Final
	private static EntityDataAccessor<Boolean> DATA_SILENT;

	@Shadow
	public abstract Level level();
	@Shadow
	public abstract SynchedEntityData getEntityData();

	@Shadow
	public abstract void discard();

	@Shadow
	public abstract void stopRiding();

	@Shadow
	public abstract void move(MoverType movementType, Vec3 movement);

	@Shadow
	public abstract Vec3 getDeltaMovement();

	@Shadow
	public abstract void setDeltaMovement(Vec3 velocity);
	@Shadow
	public abstract BlockPos blockPosition();

	@Shadow
	@Final
	protected RandomSource random;

	@Shadow
	public abstract float getBbWidth();

	@Shadow
	public abstract float getBbHeight();

	@Shadow
	public abstract double getX();

	@Shadow
	public abstract double getY();

	@Shadow
	public abstract double getZ();

	@Shadow
	public abstract EntityType<?> getType();

	@Shadow
	public abstract AABB getBoundingBox();

	@Shadow
	public abstract Vec3 position();

	@Shadow
	public int tickCount;

	@Shadow
	public abstract int getId();

	@Shadow
	public abstract BlockState getBlockStateOn();

	@Unique
	private int portalCollisionRecursionDepth;
	@Unique
	private TeleportProgressTracker teleportProgressTracker;
	@Unique
	private boolean isNextTeleportNonLocal;

	@WrapOperation(
			method = {"move", "lerpPositionAndRotationStep"},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V"
			)
	)
	private void moveThroughPortals(Entity self, double x, double y, double z, Operation<Void> original) {
		if (!PortalTeleportHandler.handle(self, x, y, z)) {
			original.call(self, x, y, z);
		}
	}

	@Redirect(
			method = "method_30022", // betweenClosedStream lambda in isInWall
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
			)
	)
	private VoxelShape provideEntityContext(BlockState instance, BlockGetter blockGetter, BlockPos blockPos) {
		return instance.getCollisionShape(blockGetter, blockPos, CollisionContext.of((Entity) (Object) this));
	}

	@Override
	public int pc$getPortalCollisionRecursionDepth() {
		return portalCollisionRecursionDepth;
	}

	@Override
	public void pc$setPortalCollisionRecursionDepth(int depth) {
		this.portalCollisionRecursionDepth = depth;
	}

	@Override
	public void pc$setNextTeleportNonLocal(boolean value) {
		this.isNextTeleportNonLocal = value;
	}

	@Override
	public boolean pc$isNextTeleportNonLocal() {
		return this.isNextTeleportNonLocal;
	}

	@Override
	public TeleportProgressTracker getTeleportProgressTracker() {
		return this.teleportProgressTracker;
	}

	@Override
	public void setTeleportProgressTracker(@Nullable TeleportProgressTracker tracker) {
		this.teleportProgressTracker = tracker;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tickTeleportTracker(CallbackInfo ci) {
		TeleportProgressTracker tracker = this.getTeleportProgressTracker();
		if (tracker != null && tracker.hasTimedOut(this.tickCount)) {
			this.setTeleportProgressTracker(null);
			// timeout. something has gone wrong, request a refresh from server
			PortalCubedPackets.sendToServer(new RequestEntitySyncPacket(this.getId()));
		}
	}

	@WrapOperation(
			method = {
					"teleportTo(DDD)V",
					"teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z"
			},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;teleportPassengers()V"
			)
	)
	private void onTeleport(Entity instance, Operation<Void> original) {
		original.call(instance);
		this.pc$setNextTeleportNonLocal(true);
	}

	@ModifyReturnValue(method = "canCollideWith", at = @At("RETURN"))
	private boolean dontCollideWithHeldProp(boolean original, @Local(ordinal = 1, argsOnly = true) Entity other) {
		return original && !((Object) this instanceof Player self && self.getHeldEntity() == other);
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
