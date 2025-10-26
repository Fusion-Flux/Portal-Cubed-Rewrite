package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.collision.EntityCollisionState;
import io.github.fusionflux.portalcubed.content.portal.collision.PortalCollisionUtils;
import io.github.fusionflux.portalcubed.content.portal.sync.EntityState;
import io.github.fusionflux.portalcubed.content.portal.sync.TeleportProgressTracker;
import io.github.fusionflux.portalcubed.framework.extension.PortalTeleportationExt;
import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import io.github.fusionflux.portalcubed.framework.shape.AabbObbCollider;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import it.unimi.dsi.fastutil.floats.FloatSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
	@Unique
	private static final ThreadLocal<EntityCollisionState> collisionState = new ThreadLocal<>();

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

	@Shadow
	public abstract AABB getBoundingBox();

	@Shadow
	public abstract Level level();

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

	@WrapMethod(method = "collide")
	private Vec3 wrapCollide(Vec3 motion, Operation<Vec3> original) {
		AABB area = this.getBoundingBox().expandTowards(motion).inflate(1e-7);

		// gets IDEA to be quiet about "impossible" conditions
		assert (Object) this instanceof Entity;

		List<PortalInstance.Holder> relevantPortals = PortalCollisionUtils.findRelevantPortalsFor((Entity) (Object) this, area);

		if (relevantPortals.isEmpty()) {
			return original.call(motion);
		}

		try {
			collisionState.set(new EntityCollisionState((Entity) (Object) this, motion, relevantPortals));
			return original.call(motion);
		} finally {
			//noinspection ThreadLocalSetWithNull - remove is slow and this codepath won't run on many threads
			collisionState.set(null);
		}
	}

	@ModifyVariable(method = "collideWithShapes", at = @At("HEAD"), argsOnly = true)
	private static Vec3 portalCollision(Vec3 motionMc, @Local(argsOnly = true) AABB bounds) {
		EntityCollisionState state = collisionState.get();
		if (state == null) {
			return motionMc;
		}

		Vector3d motion = TransformUtils.toJoml(motionMc);

		for (PortalInstance.Holder portal : state.portals) {
			// collect all boxes to collide with. always start with the perimeter
			List<OBB> boxes = new ArrayList<>(portal.portal().perimeterBoxes);

			// collide with collision on the other side
			AABB area = bounds.expandTowards(motion.x, motion.y, motion.z);
			PortalCollisionUtils.forEachBoxOnOtherSide(state.entity, portal, area, box -> {
				DebugRendering.addBox(1, box, Color.YELLOW);
				boxes.add(box);
				return true;
			});

			if (boxes.isEmpty())
				continue;

			AabbObbCollider collider = new AabbObbCollider(boxes);
			if (collider.collide(bounds, motion, state::addCollider) && motion.lengthSquared() < 1e-7) {
				return Vec3.ZERO;
			}
		}

		return TransformUtils.toMc(motion);
	}

	@ModifyReceiver(
			method = "collectCandidateStepUpHeights",
			at = @At(
					value = "INVOKE",
					target = "Lit/unimi/dsi/fastutil/floats/FloatSet;toFloatArray()[F",
					remap = false
			)
	) // ordinal on maxUpStep is not wrong, the params have flipped names
	private static FloatSet stepUpObbs(FloatSet heights, @Local(argsOnly = true) AABB bounds, @Local(argsOnly = true, ordinal = 0) float maxUpStep) {
		EntityCollisionState state = collisionState.get();
		if (state != null && state.hasColliders()) {
			AABB targetBounds = bounds.move(state.idealMotion.x, 0, state.idealMotion.z);
			AABB collisionStart = targetBounds.move(0, maxUpStep, 0);
			Vector3d motion = new Vector3d();

			double target = -maxUpStep;
			state.forEachCollider(box -> {
				motion.set(0, target, 0);
				double allowed = box.collide(collisionStart, Direction.Axis.Y, target);
				if (allowed != target) {
					double step = maxUpStep + allowed;
					heights.add((float) step);
				}
			});

			// collideWithShapes will run again after this, but we're done with this information
			state.stopCollectingColliders();
		}

		return heights;
	}
}
