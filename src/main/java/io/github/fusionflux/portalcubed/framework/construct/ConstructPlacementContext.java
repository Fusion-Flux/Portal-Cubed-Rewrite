package io.github.fusionflux.portalcubed.framework.construct;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public record ConstructPlacementContext(
		Level level, Direction clickedFace
) {
	public static ConstructPlacementContext of(UseOnContext ctx) {
		return new ConstructPlacementContext(
				ctx.getLevel(),
				ctx.getClickedFace()
		);
	}
}
