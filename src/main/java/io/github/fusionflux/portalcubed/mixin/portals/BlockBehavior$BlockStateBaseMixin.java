package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import io.github.fusionflux.portalcubed.framework.shape.Line;
import io.github.fusionflux.portalcubed.framework.util.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehavior$BlockStateBaseMixin {
	@Unique
	private static final double DIAGONAL_HALF_BLOCK = Math.sqrt(2) / 2;

	@ModifyReturnValue(
			method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
			at = @At("RETURN")
	)
	private VoxelShape quantumSpaceHole(VoxelShape shape, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (!(world instanceof Level level) || !(context instanceof EntityCollisionContext entityCtx))
			return shape;

		Entity entity = entityCtx.getEntity();
		if (entity == null)
			return shape;

		Vec3 start = PortalTeleportHandler.centerOf(entity);
		Vec3 end = start.add(entity.getDeltaMovement());
		DebugRendering.addLine(1, new Line(start, end), Color.GREEN);

		PortalHitResult hit = level.portalManager().lookup().clip(start, end, 1);
		if (!(hit instanceof PortalHitResult.Tail))
			return shape;

		return Shapes.empty();
	}
}
