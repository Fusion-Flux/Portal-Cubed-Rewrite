package io.github.fusionflux.portalcubed.mixin.props;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@Mixin(AbstractThrownPotion.class)
public abstract class AbstractThrownPotionMixin extends ThrowableItemProjectile {
	protected AbstractThrownPotionMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "affectEntitiesAround", at = @At("RETURN"))
	private void washPropsWithThrownWaterBottles(ServerLevel level, PotionContents potion, CallbackInfo ci, @Local(name = "aabb") AABB aabb) {
		for (Prop prop : this.level().getEntitiesOfClass(Prop.class, aabb, e -> e.is(PortalCubedEntityTags.CAN_BE_WASHED))) {
			double d = this.distanceToSqr(prop);
			// Vanilla does the same distance check
			if (d < 16.0) {
				prop.setDirty(false);
			}
		}
	}
}
