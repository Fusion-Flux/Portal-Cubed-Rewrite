package io.github.fusionflux.portalcubed.framework.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FakeBlockPlaceContext extends BlockPlaceContext {
	public FakeBlockPlaceContext(Level level, @Nullable Player player, InteractionHand interactionHand, ItemStack itemStack, BlockHitResult blockHitResult) {
		super(level, player, interactionHand, itemStack, blockHitResult);
		this.replaceClicked = true;
	}

	public FakeBlockPlaceContext(BlockPlaceContext ctx, BlockPos pos) {
		this(
			ctx.getLevel(), ctx.getPlayer(), ctx.getHand(), ctx.getItemInHand(),
			new BlockHitResult(
				Vec3.atCenterOf(pos).add(ctx.getClickedFace().getUnitVec3().scale(.5)),
				ctx.getClickedFace(),
				pos,
				false
			)
		);
	}
}
