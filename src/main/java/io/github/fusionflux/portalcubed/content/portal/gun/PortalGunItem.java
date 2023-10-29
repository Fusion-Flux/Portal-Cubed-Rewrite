package io.github.fusionflux.portalcubed.content.portal.gun;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.entity.Portal;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;
import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
		player.playSound(SoundEvents.ARROW_SHOOT);
		if (level instanceof ServerLevel serverLevel) {
			Vec3 velocity = player.getLookAngle().normalize().scale(PortalProjectile.SPEED);
			PortalProjectile projectile = PortalProjectile.create(serverLevel, type.defaultColor);
			projectile.setDeltaMovement(velocity);
			projectile.moveTo(player.getEyePosition());
			level.addFreshEntity(projectile);
		} else { // client-side
			player.swing(hand);
		}
	}
}
