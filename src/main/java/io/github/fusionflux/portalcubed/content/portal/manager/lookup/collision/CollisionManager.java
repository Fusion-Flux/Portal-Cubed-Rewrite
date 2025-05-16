package io.github.fusionflux.portalcubed.content.portal.manager.lookup.collision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class CollisionManager {
	private final Level level;
	private final Table<BlockPos, PortalInstance, CollisionPatch> patches = HashBasedTable.create();
	private final Multimap<BlockPos, CollisionPatch> byObservedPos = HashMultimap.create();

	public CollisionManager(Level level) {
		this.level = level;
	}

	public Collection<CollisionPatch> getPatches(BlockPos pos) {
		return this.patches.row(pos).values();
	}

	public boolean isPatched(BlockPos pos) {
		return !this.getPatches(pos).isEmpty();
	}

	public void portalsChanged(@Nullable PortalPair oldPair, @Nullable PortalPair newPair) {
		if (oldPair != null && oldPair.isLinked())
			this.removePair(oldPair);

		if (newPair != null && newPair.isLinked())
			this.addPair(newPair);
	}

	private void removePair(PortalPair pair) {
		pair.forEach(portal -> this.patches.column(portal).clear());
	}

	private void addPair(PortalPair pair) {
		PortalInstance primary = pair.primary().orElseThrow();
		PortalInstance secondary = pair.secondary().orElseThrow();
		this.addPortal(primary, secondary);
		this.addPortal(secondary, primary);
	}

	private void addPortal(PortalInstance portal, PortalInstance linked) {
		// iterate blocks behind portal and add a shape patch for each
		Iterables.transform(portal.blockModificationArea.intersectingBlocks(), BlockPos::immutable).forEach(
				pos -> this.patches.put(pos, portal, this.calculatePatch(portal, linked, pos))
		);
	}

	private CollisionPatch calculatePatch(PortalInstance portal, PortalInstance linked, BlockPos pos) {
		// collect the block positions that intersect with this pos when teleported
		OBB teleported = PortalTeleportHandler.teleportBox(
				new OBB(
						new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5),
						1, 1, 1,
						new Matrix3d()
				),
				portal, linked
		);
		List<BlockPos> blocks = new ArrayList<>();
		teleported.intersectingBlocks().forEach(intersectingBlock -> blocks.add(intersectingBlock.immutable()));
		return new CollisionPatch.Complex(this.level, pos, portal, linked, blocks);
	}
}
