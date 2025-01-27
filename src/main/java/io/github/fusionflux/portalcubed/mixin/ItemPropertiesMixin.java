package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.framework.extension.ItemPropertiesExt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.Properties.class)
public class ItemPropertiesMixin implements ItemPropertiesExt {
	@Unique
	@Nullable
	private ResourceLocation model;

	@Inject(method = "effectiveModel", at = @At("HEAD"), cancellable = true)
	private void changeDefaultModel(CallbackInfoReturnable<ResourceLocation> cir) {
		if (this.model != null) {
			cir.setReturnValue(this.model);
		}
	}

	@Override
	public Item.Properties pc$setModel(@Nullable ResourceLocation id) {
		this.model = id;
		return (Item.Properties) (Object) this;
	}
}
