package io.github.fusionflux.portalcubed.content.portal.gun;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkin;
import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.ShootPortalGunPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class PortalGunItem extends Item implements DirectClickItem {
	public PortalGunItem(Properties settings) {
		super(settings);
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
		return !miner.isCreative();
	}

	@Override
	public TriState onAttack(Level level, Player player, ItemStack stack, @Nullable HitResult hit) {
		if (player.getCooldowns().isOnCooldown(stack))
			return TriState.FALSE;

		this.playerShoot(player, stack, InteractionHand.MAIN_HAND, Polarity.PRIMARY);

		if (!level.isClientSide) {
			UseCooldown cooldown = stack.get(DataComponents.USE_COOLDOWN);
			if (cooldown != null) {
				cooldown.apply(stack, player);
			}
		}

		return TriState.TRUE;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		InteractionResult result = super.use(level, player, hand);
		if (result.consumesAction()) {
			return result;
		} else {
			this.playerShoot(player, player.getItemInHand(hand), hand, Polarity.SECONDARY);
			return InteractionResult.CONSUME;
		}
	}

	@Override
	public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
		return getGunSettings(oldStack) == getGunSettings(newStack);
	}

	public void playerShoot(Player player, ItemStack stack, InteractionHand hand, Polarity polarity) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			this.doClientShootEffects(player, stack, polarity);
		} else {
			PortalCubedPackets.sendToClients(PlayerLookup.tracking(serverPlayer), new ShootPortalGunPacket(player, polarity));
			player.setItemInHand(hand, shoot(new PortalGunShootContext(serverPlayer), stack, polarity));
		}
	}

	@Environment(EnvType.CLIENT)
	public void doClientShootEffects(Player player, ItemStack stack, Polarity polarity) {
		PortalGunSettings settings = getGunSettings(stack);
		if (settings == null)
			return;

		PortalGunSkin skin = settings.skin();
		if (skin != null) {
			skin.sounds().shootOf(polarity)
					.ifPresent(sound -> player.playSound(sound.value()));
		}
	}

	public static ItemStack shoot(PortalGunShootContext context, ItemStack stack, Polarity polarity) {
		PortalGunSettings gunSettings = stack.getOrDefault(PortalCubedDataComponents.PORTAL_GUN_SETTINGS, PortalGunSettings.DEFAULT);
		context.shoot(gunSettings.pair(), polarity, gunSettings.portalSettingsOf(polarity));
		return setGunSettings(stack, gunSettings.shoot(polarity));
	}

	@Nullable
	public static PortalGunSettings getGunSettings(ItemStack stack) {
		return stack.get(PortalCubedDataComponents.PORTAL_GUN_SETTINGS);
	}

	public static ItemStack setGunSettings(ItemStack stack, PortalGunSettings data) {
		stack.set(PortalCubedDataComponents.PORTAL_GUN_SETTINGS, data);
		return stack;
	}

	public static MutableComponent translate(String key) {
		return Component.translatable("item.portalcubed.portal_gun." + key);
	}
}
