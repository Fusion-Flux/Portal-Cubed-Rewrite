package io.github.fusionflux.portalcubed.content.portal.collision;

import net.minecraft.world.phys.shapes.VoxelShape;

public interface ShapePatch {
	VoxelShape apply(VoxelShape original);

	record Static(VoxelShape merged) implements ShapePatch {
		@Override
		public VoxelShape apply(VoxelShape original) {
			return this.merged;
		}
	}
}
