package io.github.fusionflux.portalcubed.framework.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.quiltmc.qsl.base.api.util.TriState;

/**
 * An item that directly listens for use and attacking.
 */
public interface DirectClickItem {
	/**
	 * @return DEFAULT to fall back to vanilla, FALSE to cancel, TRUE to continue to server / cancel
	 */
	TriState onAttack(Level level, Player player, ItemStack stack);

	/**
	 * @return DEFAULT to fall back to vanilla, FALSE to cancel, TRUE to continue to server / cancel
	 */
	TriState onUse(Level level, Player player, ItemStack stack, InteractionHand hand);
}
