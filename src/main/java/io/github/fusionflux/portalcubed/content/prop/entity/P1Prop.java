package io.github.fusionflux.portalcubed.content.prop.entity;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class P1Prop extends Prop {
	//arbitrary limit to prevent use against high-health mobs, for example; wardens
	private static float MAX_FALL_DAMAGE = 2 * 30;
	//makes it so it takes roughly the same amount of fall distance as portal 1 to kill a player
	private static float FALL_DAMAGE_PER_BLOCK = 2 * 1.5f;
	//makes it so the damage applies even when the collision box is outside the target
	private static double CHECK_BOX_EPSILON = 1E-7;

	public P1Prop(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@Override
	public void onCollision() {
		super.onCollision();
		var level = level();
		if (!level.isClientSide && verticalCollisionBelow) {
			int blocksFallen = Mth.ceil(fallDistance);
			if (blocksFallen > 0) {
				float damage = Math.min(FALL_DAMAGE_PER_BLOCK * blocksFallen, MAX_FALL_DAMAGE);
				var selector =
					EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(
					EntitySelector.LIVING_ENTITY_STILL_ALIVE).and(
					entity -> !(entity instanceof PlayerExt ext && ext.pc$heldProp().orElse(-1) == getId()));
				level.getEntities(this, getBoundingBox().inflate(CHECK_BOX_EPSILON), selector)
					.forEach(entity -> entity.hurt(PortalCubedDamageSources.portal1Prop(level, this, entity), damage));
			}
		}
	}
}
