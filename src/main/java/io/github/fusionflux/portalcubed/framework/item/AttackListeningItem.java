package io.github.fusionflux.portalcubed.framework.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/// An item that has custom behavior when a player attacks with it.
public interface AttackListeningItem {
	/// @return [TriState.DEFAULT] to fall back to vanilla, [TriState.FALSE] to cancel, [TriState.TRUE] to cancel and notify the server
	default TriState onAttack(Level level, Player player, ItemStack stack, @Nullable HitResult hit) {
		return TriState.DEFAULT;
	}
}
