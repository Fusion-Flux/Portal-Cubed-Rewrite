package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureMarkerMaterial;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureWrapper;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.Material;

@Mixin(TextureSlots.Resolver.class)
public class TextureSlots_ResolverMixin {
	@Inject(
			method = "resolve",
			at = @At(
					value = "INVOKE",
					target = "Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;get(Ljava/lang/Object;)Ljava/lang/Object;",
					remap = false
			)
	)
	private void insertDynamicTextureMarker(
			CallbackInfoReturnable<TextureSlots> cir,
			@Local(ordinal = 0) Object2ObjectMap<String, Material> resolved,
			@Local ObjectIterator<Object2ObjectMap.Entry<String, TextureSlots.Reference>> iterator,
			@Local Object2ObjectMap.Entry<String, TextureSlots.Reference> entry
	) {
		if (entry.getValue().target().equals(DynamicTextureWrapper.REFERENCE_MARKER)) {
			resolved.put(entry.getKey(), new DynamicTextureMarkerMaterial());
			iterator.remove();
		}
	}
}
