package io.github.fusionflux.portalcubed.content.portal.collision;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollisionManager {
	private final Level level;
	private final Map<BlockPos, ShapePatch> patches;

	public CollisionManager(Level level) {
		this.level = level;
		this.patches = new HashMap<>();
	}

	@Nullable
	public ShapePatch getPatchAt(BlockPos pos) {
		return patches.get(pos);
	}

	public void handlePortalLink(Portal a, Portal b) {
		this.handleNewPortal(a, b);
		this.handleNewPortal(b, a);
	}

	public void handlePortalUnlink(Portal a, Portal b) {
		this.removePortal(a);
		this.removePortal(b);
	}

	private void handleNewPortal(Portal portal, Portal linked) {
		// iterate through block positions in front of the output portal
		BlockPos.betweenClosedStream(linked.collisionArea).forEach(posInFront -> {
			BlockState state = level.getBlockState(posInFront);
			if (state.isAir())
				return; // easy skip, don't care
			// convert position in front of output, to a position behind the input
			Vec3 outputOriginToCorner = linked.origin.vectorTo(Vec3.atLowerCornerOf(posInFront));
			Vec3 teleported = PortalTeleportHandler.teleportRelativeVecBetween(outputOriginToCorner, linked, portal);
			Vec3 posBehindIn = portal.origin.add(teleported);
			BlockPos targetPos = BlockPos.containing(posBehindIn);
			Vec3 inputOriginToCorner = portal.origin.vectorTo(Vec3.atLowerCornerOf(targetPos));

			ShapePatch patch = ShapePatch.create(state, level, posInFront, inputOriginToCorner);
			if (patch == null)
				return;

			this.patches.put(targetPos, patch);
		});
	}

	private void removePortal(Portal portal) {
		BlockPos.betweenClosedStream(portal.blockCollisionArea).forEach(this.patches::remove);
	}
}
