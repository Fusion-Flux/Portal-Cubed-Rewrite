package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.fizzler.Disintegration;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
	protected ItemEntityMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@ModifyReturnValue(method = "hasPickUpDelay", at = @At("RETURN"))
	private boolean cantBePickedUpWhenDisintegrating(boolean original) {
		return original || Disintegration.isDisintegrating(this);
	}
}
