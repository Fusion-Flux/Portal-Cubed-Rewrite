package io.github.fusionflux.portalcubed.content.portal.gun;

import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;
import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.TriState;

public class PortalGunItem extends Item implements DirectClickItem, DyeableLeatherItem {
	public static final int DEFAULT_SHELL_COLOR = 0xFFFFFFFF;
	public static final String DATA_KEY = "portal_gun_data";

	public PortalGunItem(Properties settings) {
		super(settings);
	}

	@Override
	public TriState onAttack(Level level, Player player, ItemStack stack, @Nullable HitResult hit) {
		this.shoot(level, player, stack, InteractionHand.MAIN_HAND, PortalType.PRIMARY);
		return TriState.TRUE;
	}

	@Override
	public TriState onUse(Level level, Player player, ItemStack stack, @Nullable HitResult hit, InteractionHand hand) {
		this.shoot(level, player, stack, hand, PortalType.SECONDARY);
		return TriState.TRUE;
	}

	public void shoot(Level level, Player player, ItemStack stack, InteractionHand hand, PortalType type) {
		if (level instanceof ServerLevel serverLevel) {
			PortalGunData gunData = getData(stack);
			PortalData portalData = gunData.portalDataOf(type);
			Vec3 lookAngle = player.getLookAngle().normalize();
			Vec3 velocity = lookAngle.scale(PortalProjectile.SPEED);
			Direction horizontalFacing = Direction.getNearest(lookAngle.x, 0, lookAngle.z);

			PortalProjectile projectile = PortalProjectile.create(serverLevel, portalData, horizontalFacing);
			projectile.setDeltaMovement(velocity);
			projectile.moveTo(player.getEyePosition());
			level.addFreshEntity(projectile);
			level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS);

			PortalGunData modifiedData = gunData.withActive(type);
			ItemStack newStack = setData(stack, modifiedData);
			player.setItemInHand(hand, newStack);
		} else { // client-side
			player.swing(hand);
		}
	}

	@Override
	public int getColor(ItemStack stack) {
		int color = DyeableLeatherItem.super.getColor(stack);
		return color == DyeableLeatherItem.DEFAULT_LEATHER_COLOR ? DEFAULT_SHELL_COLOR : color;
	}

	public static PortalGunData getData(ItemStack stack) {
		CompoundTag tag = stack.getTagElement(DATA_KEY);
		return PortalGunData.CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(PortalGunData.DEFAULT);
	}

	public static ItemStack setData(ItemStack stack, PortalGunData data) {
		return PortalGunData.CODEC.encodeStart(NbtOps.INSTANCE, data).result().map(tag -> {
			ItemStack copy = stack.copy();
			copy.getOrCreateTag().put(DATA_KEY, tag);
			return copy;
		}).orElse(stack);
	}
}
