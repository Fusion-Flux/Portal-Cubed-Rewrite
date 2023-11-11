package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@WrapOperation(
			method = "move",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V"
			)
	)
	private void moveThroughPortals(Entity self, double x, double y, double z, Operation<Void> original) {
		PortalTeleportHandler.handle(self, x, y, z, original);
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
}
