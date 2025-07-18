package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.collision.PortalAwareCollisionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehavior$BlockStateBaseMixin {
	@Unique
	private static final double DIAGONAL_HALF_BLOCK = Math.sqrt(2) / 2;

	@Shadow
	public abstract Block getBlock();

	@ModifyReturnValue(
			method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
			at = @At("RETURN")
	)
	private VoxelShape quantumSpaceHole(VoxelShape shape, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (!(world instanceof Level level) || !(context instanceof PortalAwareCollisionContext aware))
			return shape;

		Vec3 start = aware.pc$pos();
		if (start == null)
			return shape;

		Vec3 thisCenter = Vec3.atCenterOf(pos);
		Vec3 offset = start.vectorTo(thisCenter).normalize().scale(DIAGONAL_HALF_BLOCK);
		Vec3 end = thisCenter.add(offset);

		PortalHitResult hit = level.portalManager().lookup().clip(start, end, 1);
		if (!(hit instanceof PortalHitResult.Tail tail))
			return shape;

		return tail.enteredPortal().portal().patchedShapes.get(level, pos, shape, context, tail.enteredPortal());
	}
}
