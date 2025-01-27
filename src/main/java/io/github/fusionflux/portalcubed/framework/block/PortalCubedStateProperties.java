package io.github.fusionflux.portalcubed.framework.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public interface PortalCubedStateProperties {
	BooleanProperty ACTIVE = BooleanProperty.create("active");
	EnumProperty<Direction> FACE = EnumProperty.create("face", Direction.class);
}
