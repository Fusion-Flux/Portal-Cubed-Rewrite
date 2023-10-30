package io.github.fusionflux.portalcubed.content.portal.gun;

import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;
import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
		if (level instanceof ServerLevel serverLevel) {
			Vec3 lookAngle = player.getLookAngle().normalize();
			Vec3 velocity = lookAngle.scale(PortalProjectile.SPEED);
			Direction horizontalFacing = Direction.getNearest(lookAngle.x, 0, lookAngle.z);
			PortalProjectile projectile = PortalProjectile.create(serverLevel, type.defaultColor, horizontalFacing);
			projectile.setDeltaMovement(velocity);
			projectile.moveTo(player.getEyePosition());
			level.addFreshEntity(projectile);
			level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS);
		} else { // client-side
			player.swing(hand);
		}
	}
}
