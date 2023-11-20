package io.github.fusionflux.portalcubed.content.portal.collision;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShapePatch {
	private final CollisionManager manager;
	private final Portal portal;
	private final BlockPos pos;

	public ShapePatch(CollisionManager manager, Portal portal, BlockPos pos) {
		this.manager = manager;
		this.portal = portal;
		this.pos = pos;
	}

	public VoxelShape apply(VoxelShape original, EntityCollisionContext ctx) {
		return manager.modifyShapeForPortal(original, portal, pos, ctx.getEntity());
	}
}
