package io.github.fusionflux.portalcubed.content.prop;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class HammerItem extends Item {
	public HammerItem(Properties settings) {
		super(settings);
	}

	public static boolean usingHammer(Player player) {
		return player.getMainHandItem().is(ConventionalItemTags.WRENCH_TOOLS);
	}
}
