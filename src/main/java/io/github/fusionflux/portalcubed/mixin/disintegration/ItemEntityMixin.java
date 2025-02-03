package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.framework.extension.DisintegrationExt;
import net.minecraft.world.entity.item.ItemEntity;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements DisintegrationExt {
	@ModifyReturnValue(method = "hasPickUpDelay", at = @At("RETURN"))
	private boolean noPickUpIfDisintegrating(boolean original) {
		return original || this.pc$disintegrating();
	}
}
