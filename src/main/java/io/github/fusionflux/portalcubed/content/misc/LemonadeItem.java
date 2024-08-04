package io.github.fusionflux.portalcubed.content.misc;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public class LemonadeItem extends Item {
	public static final float MIN_THROW_POWER = 0.375f;
	public static final float MAX_THROW_POWER = 0.8f;
	public static final String ARMED_KEY = "armed";

	public LemonadeItem(Properties settings) {
		super(settings);
	}

	public static ItemStack setArmed(boolean armed, ItemStack stack) {
		if (armed) {
			stack.getOrCreateTag().putBoolean(ARMED_KEY, true);
		} else if (stack.hasTag() && stack.getTag().contains(ARMED_KEY)) {
			stack.getTag().remove(ARMED_KEY);
		}
		return stack;
	}

	public static boolean isArmed(ItemStack stack) {
		return (stack.getItem() instanceof LemonadeItem && stack.hasTag()) && stack.getTag().getBoolean(ARMED_KEY);
	}

	public ItemStack finishArming(ItemStack stack, Level world, LivingEntity user, int armTime) {
		if (!world.isClientSide) {
			if (user instanceof Player player && !player.getAbilities().mayBuild) {
				world.playSound(null, user.getX(), user.getY(), user.getZ(), PortalCubedSounds.SURPRISE, user.getSoundSource(), 1f, 1f);
			} else {
				Lemonade lemonade = new Lemonade(world, user);
				lemonade.setItem(stack);

				lemonade.explodeTicks = stack.getUseDuration() - armTime;
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
	public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
		user.setItemInHand(user.getUsedItemHand(), finishArming(stack, world, user, user.getTicksUsingItem()));
	}

	@NotNull
 	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
		return finishArming(stack, world, user, stack.getUseDuration());
	}

	@Override
	public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		if (remainingUseTicks == Lemonade.DING_TICK) user.playSound(PortalCubedSounds.timerDing(user.getRandom()));
		if (remainingUseTicks % Lemonade.TICKS_PER_TIMER_TICK == 0 && remainingUseTicks != 0) user.playSound(PortalCubedSounds.OLD_AP_TIMER);
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);
		if (isArmed(stack))
			return InteractionResultHolder.consume(finishArming(stack, world, user, this.getUseDuration(stack)));
		setArmed(true, stack);

		// sounds
		user.playSound(SoundEvents.IRON_TRAPDOOR_OPEN, 1f, 0.7f);
		user.playSound(SoundEvents.IRON_TRAPDOOR_CLOSE, 1f, 1.7f);

		user.startUsingItem(hand);
		user.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResultHolder.consume(stack);
	}

	@Override
	public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
		return !(isArmed(oldStack) && !isArmed(newStack));
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return Lemonade.MAX_ARM_TIME;
	}

	@NotNull
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}
}
