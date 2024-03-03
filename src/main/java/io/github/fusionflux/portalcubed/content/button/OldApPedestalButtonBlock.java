package io.github.fusionflux.portalcubed.content.button;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;

public class OldApPedestalButtonBlock extends PedestalButtonBlock {
	public OldApPedestalButtonBlock(Properties properties) {
		super(properties, VoxelShaper.forHorizontal(Shapes.or(box(5.5, 0, 5.5, 10.5, 17.05, 10.5), box(6, 17, 6, 10, 19.05, 10)), Direction.UP), PortalCubedSounds.OLD_AP_PEDESTAL_BUTTON_PRESS, PortalCubedSounds.OLD_AP_PEDESTAL_BUTTON_RELEASE);
	}
}
