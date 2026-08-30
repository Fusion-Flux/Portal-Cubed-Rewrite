package io.github.fusionflux.portalcubed.mixin.goo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CauldronInteractions.class)
public interface CauldronInteractionsAccessor {
	@Invoker
	static CauldronInteraction.Dispatcher callNewDispatcher(String name) {
		throw new AbstractMethodError();
	}

	@Invoker
	static InteractionResult callDyedItemIteration(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack itemInHand) {
		throw new AbstractMethodError();
	}
}
