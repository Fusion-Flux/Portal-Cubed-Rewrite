package io.github.fusionflux.portalcubed.content.prop;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class P1Prop extends Prop {
	private static float MAX_FALL_DAMAGE = 30;
	private static float FALL_DAMAGE_PER_BLOCK = 2 * 1.5f;

	public P1Prop(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@Override
	public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		int blocksFallen = Mth.ceil(fallDistance - 1);
		if (blocksFallen < 0) {
			return false;
		} else {
			float damage = Math.min(FALL_DAMAGE_PER_BLOCK * blocksFallen, MAX_FALL_DAMAGE);
			var selector = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
			var level = level();
			level.getEntities(this, getBoundingBox(), selector).forEach(entity -> entity.hurt(PortalCubedDamageSources.portal1Prop(level), damage));
			return false;
		}
	}
}
