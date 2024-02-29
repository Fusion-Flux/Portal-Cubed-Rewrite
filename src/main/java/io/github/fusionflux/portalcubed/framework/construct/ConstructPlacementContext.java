package io.github.fusionflux.portalcubed.framework.construct;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ConstructPlacementContext(
		Level level, Direction clickedFace, Direction placerFacing
) {
	public static ConstructPlacementContext of(UseOnContext ctx) {
		Direction clickedFace = ctx.getClickedFace();

		Player player = ctx.getPlayer();
		Direction placerFacing = clickedFace.getOpposite();
		if (player != null) {
			Vec3 lookVec = player.getLookAngle();
			placerFacing = Direction.getNearest(lookVec.x, lookVec.y, lookVec.z);
		}

		return new ConstructPlacementContext(
				ctx.getLevel(),
				clickedFace,
				placerFacing
		);
	}
}
