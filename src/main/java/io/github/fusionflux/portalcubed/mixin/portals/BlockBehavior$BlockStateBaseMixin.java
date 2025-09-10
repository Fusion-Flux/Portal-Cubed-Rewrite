package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehavior$BlockStateBaseMixin {
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

		AABB area = new AABB(pos).minmax(entity.getBoundingBox());
		List<PortalInstance.Holder> portals = level.portalManager().lookup().getPortals(area);
		if (portals.isEmpty())
			return shape;

		Vec3 thisCenter = Vec3.atCenterOf(pos);

		for (PortalInstance.Holder holder : portals) {
			if (holder.opposite().isEmpty())
				continue;

			PortalInstance portal = holder.portal();
			if (portal.plane.isBehind(thisCenter) && portal.seesModifiedCollision(entity)) {
				// at least one open portal pair between, discard this collision
				return Shapes.empty();
			}
		}

		return shape;
	}
}
