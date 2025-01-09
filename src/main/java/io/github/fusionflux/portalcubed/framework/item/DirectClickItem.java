package io.github.fusionflux.portalcubed.framework.item;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.TriState;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * An item that directly listens for use and attacking.
 */
public interface DirectClickItem {
	/**
	 * @return DEFAULT to fall back to vanilla, FALSE to cancel, TRUE to continue to server / cancel
	 */
	TriState onAttack(Level level, Player player, ItemStack stack, @Nullable HitResult hit);

	/**
	 * @return DEFAULT to fall back to vanilla, FALSE to cancel, TRUE to continue to server / cancel
	 */
	TriState onUse(Level level, Player player, ItemStack stack, @Nullable HitResult hit, InteractionHand hand);
}
