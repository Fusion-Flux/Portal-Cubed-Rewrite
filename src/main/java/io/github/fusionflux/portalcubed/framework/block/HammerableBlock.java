package io.github.fusionflux.portalcubed.framework.block;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

public interface HammerableBlock {
	@NotNull
	InteractionResult onHammered(BlockState state, Level world, BlockPos pos, Player player);

	static void registerEventListeners() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!stack.is(PortalCubedItemTags.WRENCHES))
				return InteractionResult.PASS;

			if (!player.mayBuild())
				return InteractionResult.PASS;

			BlockPos pos = hitResult.getBlockPos();
			BlockState state = world.getBlockState(pos);
			if (!(state.getBlock() instanceof HammerableBlock hammerable))
				return InteractionResult.PASS;

			InteractionResult result = hammerable.onHammered(state, world, pos, player);
			if (result.shouldAwardStats())
				player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
			return result;
		});
	}
}
