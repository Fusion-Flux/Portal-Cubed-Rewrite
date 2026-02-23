package io.github.fusionflux.portalcubed.content.portal.interaction;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Interface that may be implemented on {@link Item}s to allow them to be used on portals.
 */
public interface UsableOnPortals {
	/**
	 * Use this item on a portal.
	 */
	InteractionResult useOnPortal(Player user, PortalReference portal, ItemStack stack, InteractionHand hand);
}
