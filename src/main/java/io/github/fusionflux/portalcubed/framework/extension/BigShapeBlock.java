package io.github.fusionflux.portalcubed.framework.extension;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface BigShapeBlock {
	@ClientOnly
	static void pick(double reach, float tickDelta) {
		var client = Minecraft.getInstance();
		if (!(client.cameraEntity instanceof Player player && player.isLocalPlayer()))
			return;
		var level = client.level;

		var start = player.getEyePosition(tickDelta);
		reach = (client.hitResult != null && client.hitResult.getType() != HitResult.Type.MISS) ? client.hitResult.getLocation().distanceTo(start) : reach;
		var end = start.add(player.getViewVector(tickDelta).scale(reach));

		var clipContext = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
		var result = BlockGetter.traverseBlocks(start, end, clipContext, ($, pos) -> {
			BlockHitResult currentHit = null;
			for (var cur : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
				var state = level.getBlockState(cur);
				if (state.getBlock() instanceof BigShapeBlock) {
					var hit = state.getShape(level, cur).clip(start, end, cur);
					if (hit == null || hit.getType() == HitResult.Type.MISS) continue;
					if (currentHit != null && Vec3.atCenterOf(cur).distanceToSqr(start) >= Vec3.atCenterOf(currentHit.getBlockPos()).distanceTo(start)) continue;
					currentHit = new BlockHitResult(Vec3.atCenterOf(cur), hit.getDirection(), cur.immutable(), hit.isInside());
				}
			}
			return currentHit;
		}, $ -> {
			var dir = start.subtract(end);
			return BlockHitResult.miss(end, Direction.getNearest(dir.x, dir.y, dir.z), BlockPos.containing(end));
		});

		if (result != null && result.getType() != HitResult.Type.MISS)
			client.hitResult = result;
	}
}
