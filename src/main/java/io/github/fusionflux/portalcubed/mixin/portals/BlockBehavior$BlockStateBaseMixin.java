package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import io.github.fusionflux.portalcubed.framework.util.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehavior$BlockStateBaseMixin {
	@Shadow
	public abstract boolean is(TagKey<Block> tag, Predicate<BlockBehaviour.BlockStateBase> predicate);

	@ModifyReturnValue(
			method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
			at = @At("RETURN")
	)
	private VoxelShape quantumSpaceHole(VoxelShape shape, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (!(world instanceof Level level) || !(context instanceof EntityCollisionContext entityCtx) || shape.isEmpty())
			return shape;

		Entity entity = entityCtx.getEntity();
		if (entity == null)
			return shape;

		AABB posAabb = new AABB(pos);
		AABB area = posAabb.minmax(entity.getBoundingBox());
		DebugRendering.addBox(1, area, Color.ORANGE);
		List<PortalInstance.Holder> portals = level.portalManager().lookup().getPortals(area);
		if (portals.isEmpty())
			return shape;

		for (PortalInstance.Holder holder : portals) {
			if (holder.opposite().isEmpty())
				continue;

			PortalInstance portal = holder.portal();
			if (portal.plane.isFullyBehindOrOn(posAabb) && portal.seesModifiedCollision(entity)) {
				return Shapes.empty();
			}
		}

		return shape;
	}
}
