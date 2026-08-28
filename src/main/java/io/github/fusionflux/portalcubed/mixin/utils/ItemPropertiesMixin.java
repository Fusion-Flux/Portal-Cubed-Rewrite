package io.github.fusionflux.portalcubed.mixin.utils;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.framework.extension.ItemPropertiesExt;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

@Mixin(Item.Properties.class)
public class ItemPropertiesMixin implements ItemPropertiesExt {
	@Unique
	@Nullable
	private Identifier model;

	@Inject(method = "effectiveModel", at = @At("HEAD"), cancellable = true)
	private void changeDefaultModel(CallbackInfoReturnable<Identifier> cir) {
		if (this.model != null) {
			cir.setReturnValue(this.model);
		}
	}

	@Override
	public Item.Properties pc$setModel(@Nullable Identifier id) {
		this.model = id;
		return (Item.Properties) (Object) this;
	}
}
