package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.sync.TeleportProgressTracker;
import io.github.fusionflux.portalcubed.framework.extension.PortalTeleportationExt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(Entity.class)
public abstract class EntityMixin implements PortalTeleportationExt {
	@Shadow
	public int tickCount;

	@Shadow
	public abstract int getId();

	@Unique
	private int portalCollisionRecursionDepth;
	@Unique
	private TeleportProgressTracker teleportProgressTracker = new TeleportProgressTracker((Entity) (Object) this);
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

	@Inject(method = "tick", at = @At("TAIL"))
	private void tickTeleportTracker(CallbackInfo ci) {
		this.getTeleportProgressTracker().tick();
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
}
