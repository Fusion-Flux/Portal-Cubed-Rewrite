package io.github.fusionflux.portalcubed.content.misc;

import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;

// Logic in RangedBowAttackGoalMixin
public class LemonadeAttackGoal<T extends Monster & RangedAttackMob> extends RangedBowAttackGoal<T> {
	public int timeUntilThrow = 0;

	public LemonadeAttackGoal(T actor, double speed, int attackInterval, float range) {
		super(actor, speed, attackInterval, range);
	}
}
