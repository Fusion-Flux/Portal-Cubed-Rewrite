package io.github.fusionflux.portalcubed.content.button;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;

public class CubeButtonBlock extends FloorButtonBlock {
	public CubeButtonBlock(Properties properties) {
		super(properties, new VoxelShaper[][]{
			new VoxelShaper[]{
				VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(6, 1, 6, 16, 3, 16)), Direction.UP),
				VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(0, 1, 6, 10, 3, 16)), Direction.UP)
			},
			new VoxelShaper[]{
				VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(6, 1, 0, 16, 3, 10)), Direction.UP),
				VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(0, 1, 0, 10, 3, 10)), Direction.UP)
			}
		}, box(9.5, 9.5, 3, 16, 16, 6), entity -> entity.getType().is(PortalCubedEntityTags.PRESSES_CUBE_BUTTONS), PortalCubedSounds.FLOOR_BUTTON_PRESS, PortalCubedSounds.FLOOR_BUTTON_RELEASE);
	}
}
