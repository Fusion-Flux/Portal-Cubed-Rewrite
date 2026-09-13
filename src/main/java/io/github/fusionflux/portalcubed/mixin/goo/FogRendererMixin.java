package io.github.fusionflux.portalcubed.mixin.goo;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.fusionflux.portalcubed.content.goo.GooFogEnvironment;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@ModifyExpressionValue(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;"
			)
	)
	private static ArrayList<FogEnvironment> addGooFogEnvironment(ArrayList<FogEnvironment> list) {
		list.add(GooFogEnvironment.INSTANCE);
		return list;
	}
}
