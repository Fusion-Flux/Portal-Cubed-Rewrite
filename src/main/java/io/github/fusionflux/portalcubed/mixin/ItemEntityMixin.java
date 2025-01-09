package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
	@Inject(method = "hasPickUpDelay", at = @At("RETURN"), cancellable = true)
	private void noPickUpIfDisintegrating(CallbackInfoReturnable<Boolean> cir) {
		if (((Entity) (Object) this).pc$disintegrating()) cir.setReturnValue(true);
	}
}
