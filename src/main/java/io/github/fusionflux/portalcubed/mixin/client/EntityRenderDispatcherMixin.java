package io.github.fusionflux.portalcubed.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationVertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@ModifyArg(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
			)
	)
	private MultiBufferSource useDisintegrationBufferSource(MultiBufferSource bufferSource, @Local(argsOnly = true) Entity entity, @Local(ordinal = 1, argsOnly = true) float tickDelta) {
		if (entity.pc$disintegrating())
			return renderType -> new DisintegrationVertexConsumer(bufferSource.getBuffer(renderType), entity.pc$disintegrateTicks() + tickDelta);
		return bufferSource;
	}
}
