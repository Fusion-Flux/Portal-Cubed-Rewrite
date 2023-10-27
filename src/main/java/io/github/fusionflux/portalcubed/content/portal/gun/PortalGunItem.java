package io.github.fusionflux.portalcubed.content.portal.gun;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.entity.Portal;
import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.quiltmc.qsl.base.api.util.TriState;

public class PortalGunItem extends Item implements DirectClickItem {
	public PortalGunItem(Properties settings) {
		super(settings);
	}

	@Override
	public TriState onAttack(Level level, Player player, ItemStack stack) {
		this.shoot(level, player, stack, InteractionHand.MAIN_HAND, PortalType.PRIMARY);
		return TriState.TRUE;
	}

	@Override
	public TriState onUse(Level level, Player player, ItemStack stack, InteractionHand hand) {
		this.shoot(level, player, stack, hand, PortalType.SECONDARY);
		return TriState.TRUE;
	}

	public void shoot(Level level, Player player, ItemStack stack, InteractionHand hand, PortalType type) {
		Portal portal = new Portal(PortalCubedEntities.PORTAL, level);
		portal.moveTo(player.position());
		level.addFreshEntity(portal);
	}
}
