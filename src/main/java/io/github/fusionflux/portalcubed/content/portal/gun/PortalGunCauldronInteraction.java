package io.github.fusionflux.portalcubed.content.portal.gun;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record PortalGunCauldronInteraction(CauldronInteraction doFirst) implements CauldronInteraction {
	@Override
	public InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
		InteractionResult result = this.doFirst.interact(state, level, pos, player, hand, stack);
		if (!result.consumesAction())
			return result;

		if (!level.isClientSide) {
			player.pc$setHasSubmergedTheOperationalEndOfTheDevice(true);
			player.pc$disintegrate();
			FallingBlockEntity cauldron = FallingBlockEntity.fall(level, pos, state);
			cauldron.pc$disintegrate();
		}

		return InteractionResult.SUCCESS;

	}
}
