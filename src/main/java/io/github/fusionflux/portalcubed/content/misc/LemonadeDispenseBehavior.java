package io.github.fusionflux.portalcubed.content.misc;

import net.minecraft.Util;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public class LemonadeDispenseBehavior extends AbstractProjectileDispenseBehavior {
	@NotNull
	@Override
	protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
		return Util.make(new Lemonade(world, position.x(), position.y(), position.z()), lemonade -> {
			lemonade.setItem(LemonadeItem.setArmed(true, stack));
			lemonade.explodeTicks = LemonadeItem.MAX_ARM_TIME;
		});
	}

	@Override
	protected float getPower() {
		return LemonadeItem.MAX_THROW_POWER;
	}
}
