package io.github.fusionflux.portalcubed.mixin.props;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownPotion;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ThrownPotion.class)
public abstract class ThrowPotionMixin extends ThrowableItemProjectile  {
	@Unique
	private static final Predicate<Prop> IS_PROP = e -> e.getType().is(PortalCubedEntityTags.CAN_BE_WASHED) && PortalCubedEntities.PROPS.containsValue(e.getType());

	protected ThrowPotionMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(
		method = "applyWater",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/ThrownPotion;level()Lnet/minecraft/world/level/Level;",
			ordinal = 0
		)
	)
	private void washPropsWithThrownWaterBottles(ServerLevel level, CallbackInfo ci, @Local AABB aabb) {
		for (Prop prop : this.level().getEntitiesOfClass(Prop.class, aabb, IS_PROP)) {
			double d = this.distanceToSqr(prop);
			if (d < 16.0) {
				prop.setDirty(false);
			}
		}
	}
}
