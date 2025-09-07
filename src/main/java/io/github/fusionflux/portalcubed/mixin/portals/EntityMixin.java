package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.sync.EntityState;
import io.github.fusionflux.portalcubed.content.portal.sync.TeleportProgressTracker;
import io.github.fusionflux.portalcubed.framework.extension.PortalTeleportationExt;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(Entity.class)
public abstract class EntityMixin implements PortalTeleportationExt {
	@Shadow
	public abstract int getId();

	@Shadow
	public abstract float getEyeHeight();

	@Shadow
	public abstract Vec3 getDeltaMovement();

	@Shadow
	protected abstract Vec3 collide(Vec3 vec);

	@Shadow
	public abstract Vec3 position();

	@Unique
	private final TeleportProgressTracker teleportProgressTracker = new TeleportProgressTracker((Entity) (Object) this);

	@Unique
	private int portalCollisionRecursionDepth;
	@Unique
	private boolean isNextTeleportNonLocal;

	@WrapOperation(
			method = "move",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V",
					ordinal = 0
			)
	)
	private void moveThroughPortalsNoPhysics(Entity self, double x, double y, double z, Operation<Void> original,
											 @Local(argsOnly = true) LocalRef<Vec3> movement) {
		Vec3 oldPos = self.position();
		original.call(self, x, y, z);
		if (PortalTeleportHandler.handle(self, oldPos)) {
			// need to update the values that were used to move to this new pos
			Vec3 newVel = this.getDeltaMovement();
			movement.set(newVel);
		}
	}

	@WrapOperation(
			method = "move",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V",
					ordinal = 1
			)
	)
	private void moveThroughPortalsNormally(Entity self, double x, double y, double z, Operation<Void> original,
											@Local(argsOnly = true) LocalRef<Vec3> movement,
											@Local(ordinal = 1) LocalRef<Vec3> collide) {
		Vec3 oldPos = self.position();
		original.call(self, x, y, z);
		if (PortalTeleportHandler.handle(self, oldPos)) {
			// need to update the values that were used to move to this new pos
			Vec3 newVel = this.getDeltaMovement();
			movement.set(newVel);
			collide.set(this.collide(newVel));
		}
	}

	@WrapOperation(
			method = "lerpPositionAndRotationStep",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V"
			)
	)
	private void lerpThroughPortals(Entity self, double x, double y, double z, Operation<Void> original) {
		Vec3 oldPos = self.position();
		original.call(self, x, y, z);
		PortalTeleportHandler.handle(self, oldPos);
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
		return this.portalCollisionRecursionDepth;
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

	@Inject(method = "getLightProbePosition", at = @At("HEAD"), cancellable = true)
	private void probeCorrectLightWhileTeleporting(float partialTicks, CallbackInfoReturnable<Vec3> cir) {
		EntityState override = this.teleportProgressTracker.getEntityStateOverride(partialTicks);
		if (override != null) {
			cir.setReturnValue(override.pos().add(0, this.getEyeHeight(), 0));
		}
	}

	@WrapOperation(
			method = "collide",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;collideBoundingBox(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private Vec3 portalCollision(@Nullable Entity entity, Vec3 vec, AABB bounds, Level level, List<VoxelShape> shapes, Operation<Vec3> original) {
		Vec3 motion = original.call(entity, vec, bounds, level, shapes);
		if (motion.lengthSqr() == 0)
			return Vec3.ZERO;

		double motionLength = motion.length() + Math.sqrt(bounds.getXsize() * bounds.getXsize() + bounds.getZsize() * bounds.getZsize());
		Vec3 start = PortalTeleportHandler.centerOf((Entity) (Object) this);
		Vec3 end = start.add(motion.normalize().scale(motionLength));
		PortalHitResult hit = level.portalManager().lookup().clip(start, end, 1);
		if (!(hit instanceof PortalHitResult.Tail tail))
			return motion;

		PortalInstance portal = tail.enteredPortal().portal();

		for (OBB collisionBox : portal.perimeterBoxes) {
			Vec3 limited = collisionBox.collideOnAxis(bounds, motion);
			if (limited != null) {
				if (limited.lengthSqr() < 1e-7) {
					return Vec3.ZERO;
				}

				motion = limited;
			}
		}

		return motion;
	}
}
