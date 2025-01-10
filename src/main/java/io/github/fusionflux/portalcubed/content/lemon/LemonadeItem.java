package io.github.fusionflux.portalcubed.content.lemon;

import org.jetbrains.annotations.NotNull;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

public class LemonadeItem extends Item implements ProjectileItem {
	public static final float MIN_THROW_POWER = 0.375f;
	public static final float MAX_THROW_POWER = 0.8f;

	public static final DispenseConfig DISPENSE_CONFIG = DispenseConfig.builder()
			.power(MAX_THROW_POWER)
			.build();

	public LemonadeItem(Properties settings) {
		super(settings);
	}

	public static ItemStack setArmed(boolean armed, ItemStack stack) {
		if (armed) {
			stack.set(PortalCubedDataComponents.LEMONADE_ARMED, Unit.INSTANCE);
		} else {
			stack.remove(PortalCubedDataComponents.LEMONADE_ARMED);
		}
		return stack;
	}

	public static boolean isArmed(ItemStack stack) {
		return stack.has(PortalCubedDataComponents.LEMONADE_ARMED);
	}

	public ItemStack finishArming(ItemStack stack, Level world, LivingEntity user, int armTime) {
		if (!world.isClientSide) {
			if (user instanceof Player player && !player.getAbilities().mayBuild) {
				world.playSound(null, user.getX(), user.getY(), user.getZ(), PortalCubedSounds.SURPRISE, user.getSoundSource(), 1f, 1f);
			} else {
				Lemonade lemonade = new Lemonade(user, world, stack);

				lemonade.explodeTicks = stack.getUseDuration(user) - armTime;
				float power = Mth.lerp(Math.min(1, armTime / Lemonade.TICKS_PER_TIMER_TICK), MIN_THROW_POWER, MAX_THROW_POWER);
				lemonade.shootFromRotation(user, user.getXRot(), user.getYRot(), 0f, power, 1f);

				if (world.addFreshEntity(lemonade) && (!(user instanceof Player player) || !player.isCreative()))
					return ItemStack.EMPTY;
			}
			setArmed(false, stack);
		}
		return stack;
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
		ItemStack remainder = this.finishArming(stack, world, user, user.getTicksUsingItem());
		user.setItemInHand(user.getUsedItemHand(), remainder);
		return remainder.isEmpty();
	}

	@NotNull
 	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
		return this.finishArming(stack, world, user, stack.getUseDuration(user));
	}

	@Override
	public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		if (remainingUseTicks == Lemonade.DING_TICK) user.playSound(PortalCubedSounds.timerDing(user.getRandom()));
		if (remainingUseTicks % Lemonade.TICKS_PER_TIMER_TICK == 0 && remainingUseTicks != 0) user.playSound(PortalCubedSounds.OLD_AP_TIMER);
	}

	@NotNull
	@Override
	public InteractionResult use(Level world, Player user, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);
		if (isArmed(stack)) {
			ItemStack remainder = this.finishArming(stack, world, user, this.getUseDuration(stack, user));
			return InteractionResult.CONSUME.heldItemTransformedTo(remainder);
		}
		setArmed(true, stack);

		// sounds
		user.playSound(SoundEvents.IRON_TRAPDOOR_OPEN, 1f, 0.7f);
		user.playSound(SoundEvents.IRON_TRAPDOOR_CLOSE, 1f, 1.7f);

		user.startUsingItem(hand);
		user.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResult.CONSUME;
	}

	@Override
	public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
		return !(isArmed(oldStack) && !isArmed(newStack));
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return Lemonade.MAX_ARM_TIME;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BOW;
	}

	@Override
	public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
		Lemonade lemonade = new Lemonade(level, pos.x(), pos.y(), pos.z(), setArmed(true, stack));
		lemonade.explodeTicks = Lemonade.MAX_ARM_TIME;
		return lemonade;
	}

	@Override
	public DispenseConfig createDispenseConfig() {
		return DISPENSE_CONFIG;
	}

	public static void registerEventListeners() {
		ServerLivingEntityEvents.AFTER_DEATH.register((killed, source) -> {
			ItemStack useItem = killed.getUseItem();
			if (LemonadeItem.isArmed(useItem))
				killed.releaseUsingItem();
		});
	}
}
