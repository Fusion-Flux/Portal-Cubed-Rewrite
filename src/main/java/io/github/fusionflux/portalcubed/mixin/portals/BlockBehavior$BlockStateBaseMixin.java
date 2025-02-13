package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.collision.CollisionPatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehavior$BlockStateBaseMixin {
	@Shadow
	public abstract Block getBlock();

	/*
	for portals to be enterable, they need to change the collision of the blocks they're on to reflect the
	environment on the other side.

	This change is only present for entities that are touching a bounding box in front of the portal. This is
	done so that you can't walk into walls surrounding or behind the portal.

	This needs to be handled here, from a block's perspective.
	1. First, lookup collision patches for this position. If there's none, we're done.
	2. If there are any, iterate them to find one with a portal that affects the given entity.
	   If there's none, also do nothing.
	3. If one is found, apply the patch to this block's shape.
	 */
	@ModifyReturnValue(
			method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
			at = @At("RETURN")
	)
	private VoxelShape quantumSpaceHole(VoxelShape shape, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (!(world instanceof Level level) || !(context instanceof EntityCollisionContext entityCtx) || entityCtx.getEntity() == null)
			return shape;

		Entity entity = entityCtx.getEntity();
		if (PortalTeleportHandler.ignoresPortalModifiedCollision(entity))
			return shape;

		Collection<CollisionPatch> patches = level.portalManager().activePortals().collisionManager().getPatches(pos);
		if (patches.isEmpty())
			return shape;

		for (CollisionPatch patch : patches) {
			if (patch.appliesTo(entity)) {
				shape = patch.apply(shape, entityCtx);
			}
		}

		return shape;
	}
}
