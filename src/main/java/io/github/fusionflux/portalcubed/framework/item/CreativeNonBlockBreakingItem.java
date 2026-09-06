package io.github.fusionflux.portalcubed.framework.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/// An item that won't break blocks in creative mode, without the [DataComponents#TOOL] component.
public abstract class CreativeNonBlockBreakingItem extends Item {
	protected CreativeNonBlockBreakingItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
		if (user instanceof Player player) {
			return !player.getAbilities().instabuild;
		}

		return super.canDestroyBlock(stack, state, level, pos, user);
	}
}
