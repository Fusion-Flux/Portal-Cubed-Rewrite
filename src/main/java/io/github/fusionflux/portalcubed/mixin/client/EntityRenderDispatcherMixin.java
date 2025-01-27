package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
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

	@WrapOperation(
			method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
			)
	)
	private void disintegrationRendering(
			EntityRenderer<Entity, EntityRenderState> instance,
			EntityRenderState renderState,
			PoseStack matrices,
			MultiBufferSource bufferSource,
			int packedLight,
			Operation<Void> original,
			@Local(argsOnly = true) Entity entity,
			@Local(argsOnly = true) float tickDelta
	) {
		if (entity.pc$disintegrating()) {
			DisintegrationRenderer.renderFlash(entity, matrices, tickDelta, bufferSource);
			DisintegrationRenderer.wrapRender(
					Mth.clampedLerp(entity.pc$disintegrateTicks() - 1, entity.pc$disintegrateTicks(), tickDelta),
					disintegratingBufferSource -> original.call(instance, renderState, matrices, disintegratingBufferSource, packedLight)
			);
		} else {
			original.call(instance, renderState, matrices, bufferSource, packedLight);
		}
	}
}
