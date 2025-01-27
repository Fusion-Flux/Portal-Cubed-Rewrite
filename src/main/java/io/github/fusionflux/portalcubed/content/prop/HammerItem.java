package io.github.fusionflux.portalcubed.content.prop;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class HammerItem extends Item {
	public HammerItem(Properties settings) {
		super(settings);
	}

	public static boolean usingHammer(Player player) {
		return player.getMainHandItem().is(PortalCubedItemTags.WRENCH);
	}
}
