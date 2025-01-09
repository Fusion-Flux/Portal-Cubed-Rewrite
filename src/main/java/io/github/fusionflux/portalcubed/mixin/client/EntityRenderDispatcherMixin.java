package io.github.fusionflux.portalcubed.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@ModifyArgs(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
			)
	)
	private void disintegrationRendering(Args args) {
		Entity entity = args.get(0);
		if (entity.pc$disintegrating()) {
			float tickDelta = args.get(2);
			PoseStack matrices = args.get(3);
			MultiBufferSource vertexConsumers = args.get(4);

			DisintegrationRenderer.renderFlash(entity, matrices, tickDelta, vertexConsumers);
			args.set(2, 0f); // freeze tick delta
			args.set(4, DisintegrationRenderer.wrapVertexConsumers(entity, tickDelta, vertexConsumers));
		}
	}
}
