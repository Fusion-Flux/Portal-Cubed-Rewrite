package io.github.fusionflux.portalcubed.content.button;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper;
import net.minecraft.core.Direction;

public class OldApFloorButtonBlock extends FloorButtonBlock {
	public OldApFloorButtonBlock(Properties properties) {
		super(properties, new VoxelShaper[][]{
			new VoxelShaper[]{
				VoxelShaper.forDirectional(box(2, 0, 2, 16, 2, 16), Direction.UP),
				VoxelShaper.forDirectional(box(0, 0, 2, 14, 2, 16), Direction.UP)
			},
			new VoxelShaper[]{
				VoxelShaper.forDirectional(box(2, 0, 0, 16, 2, 14), Direction.UP),
				VoxelShaper.forDirectional(box(0, 0, 0, 14, 2, 14), Direction.UP)
			}
		}, box(4, 4, 2, 16, 16, 3), PortalCubedSounds.OLD_AP_FLOOR_BUTTON_PRESS, PortalCubedSounds.OLD_AP_FLOOR_BUTTON_RELEASE);
	}
}
