package io.github.fusionflux.portalcubed.framework.block;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface HammerableBlock {
	InteractionResult onHammered(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit);

	static void registerEventListeners() {
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!stack.is(ConventionalItemTags.WRENCH_TOOLS) || !player.mayBuild() || player.getCooldowns().isOnCooldown(stack))
				return InteractionResult.PASS;

			BlockPos pos = hit.getBlockPos();
			BlockState state = world.getBlockState(pos);
			if (!(state.getBlock() instanceof HammerableBlock hammerable))
				return InteractionResult.PASS;

			InteractionResult result = hammerable.onHammered(state, world, pos, player, hit);
			if (result instanceof InteractionResult.Success success && success.wasItemInteraction()) {
				player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
				UseCooldown cooldown = stack.get(DataComponents.USE_COOLDOWN);
				if (cooldown != null) {
					cooldown.apply(stack, player);
				}
			}
			return result;
		});
	}
}
