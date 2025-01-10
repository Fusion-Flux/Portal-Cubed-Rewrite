package io.github.fusionflux.portalcubed.framework.extension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface BigShapeBlock {
	@Environment(EnvType.CLIENT)
	static void pick(double reach, float tickDelta) {
		Minecraft client = Minecraft.getInstance();
		if (!(client.cameraEntity instanceof Player player && player.isLocalPlayer()))
			return;
		Level world = client.level;
		if (world == null)
			return;

		Vec3 start = player.getEyePosition(tickDelta);
		reach = (client.hitResult != null && client.hitResult.getType() != HitResult.Type.MISS) ? client.hitResult.getLocation().distanceTo(start) : reach;
		Vec3 end = start.add(player.getViewVector(tickDelta).scale(reach));

		HitResult result = BlockGetter.traverseBlocks(start, end, new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player), ($, pos) -> {
			BlockHitResult currentHit = null;
			for (BlockPos cur : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
				BlockState state = world.getBlockState(cur);
				if (state.getBlock() instanceof BigShapeBlock) {
					BlockHitResult hit = state.getShape(world, cur).clip(start, end, cur);
					if (hit == null || hit.getType() == HitResult.Type.MISS) continue;
					if (currentHit != null && Vec3.atCenterOf(cur).distanceToSqr(start) >= Vec3.atCenterOf(currentHit.getBlockPos()).distanceTo(start))
						continue;
					currentHit = new BlockHitResult(Vec3.atCenterOf(cur), hit.getDirection(), cur.immutable(), hit.isInside());
				}
			}
			return currentHit;
		}, $ -> {
			Vec3 dir = start.subtract(end);
			return BlockHitResult.miss(end, Direction.getApproximateNearest(dir.x, dir.y, dir.z), BlockPos.containing(end));
		});

		if (result.getType() != HitResult.Type.MISS) client.hitResult = result;
	}
}
