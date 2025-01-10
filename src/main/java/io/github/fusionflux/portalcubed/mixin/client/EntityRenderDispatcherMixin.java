package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@ModifyArg(
			method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
			),
			index = 1
	)
	private float freezeTickDelta(float partialTick, @Local(argsOnly = true) Entity entity) {
		if (entity.pc$disintegrating())
			return 0f;
		return partialTick;
	}

	@ModifyArg(
			method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
			),
			index = 2
	)
	private MultiBufferSource disintegrationRendering(MultiBufferSource bufferSource, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) float tickDelta, @Local(argsOnly = true) PoseStack matrices) {
		if (entity.pc$disintegrating()) {
			DisintegrationRenderer.renderFlash(entity, matrices, tickDelta, bufferSource);
			return DisintegrationRenderer.wrapVertexConsumers(entity, tickDelta, bufferSource);
		}

		return bufferSource;
	}
}
