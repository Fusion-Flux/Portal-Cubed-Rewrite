package io.github.fusionflux.portalcubed.content.portal.gun;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;
import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PortalGunItem extends Item implements DirectClickItem {
	public static final int DEFAULT_SHELL_COLOR = 0xFFFFFFFF;

	public PortalGunItem(Properties settings) {
		super(settings);
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
		return !miner.isCreative();
	}

	@Override
	public TriState onAttack(Level level, Player player, ItemStack stack, @Nullable HitResult hit) {
		this.shoot(level, player, stack, InteractionHand.MAIN_HAND, Polarity.PRIMARY);
		return TriState.TRUE;
	}

	@Override
	public TriState onUse(Level level, Player player, ItemStack stack, @Nullable HitResult hit, InteractionHand hand) {
		this.shoot(level, player, stack, hand, Polarity.SECONDARY);
		return TriState.TRUE;
	}

	public void shoot(Level level, Player player, ItemStack stack, InteractionHand hand, Polarity polarity) {
		if (level instanceof ServerLevel serverLevel) {
			PortalGunSettings gunSettings = getGunSettings(stack);
			PortalSettings portalSettings = gunSettings.portalSettingsOf(polarity);

			Vec3 lookAngle = player.getLookAngle();
			Vec3 velocity = lookAngle.scale(PortalProjectile.SPEED);
			float yRot = player.getYRot() + 180;
			String pair = gunSettings.pair().orElse(player.getGameProfile().getName());

			PortalProjectile projectile = new PortalProjectile(level, portalSettings, yRot, pair, polarity);
			projectile.setDeltaMovement(velocity);
			projectile.moveTo(player.getEyePosition());
			level.addFreshEntity(projectile);
			level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS);

			PortalGunSettings modifiedData = gunSettings.withActive(polarity);
			ItemStack newStack = setGunSettings(stack, modifiedData);
			player.setItemInHand(hand, newStack);
		} else { // client-side
			player.swing(hand);
		}
	}

	public static PortalGunSettings getGunSettings(ItemStack stack) {
		return stack.getOrDefault(PortalCubedDataComponents.PORTAL_GUN_SETTINGS, PortalGunSettings.DEFAULT);
	}

	public static ItemStack setGunSettings(ItemStack stack, PortalGunSettings data) {
		stack.set(PortalCubedDataComponents.PORTAL_GUN_SETTINGS, data);
	}
}
