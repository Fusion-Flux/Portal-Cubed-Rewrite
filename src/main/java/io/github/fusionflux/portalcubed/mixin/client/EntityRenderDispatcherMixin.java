package io.github.fusionflux.portalcubed.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationVertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@ModifyArgs(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
			)
	)
	private void disintegrationRendering(
			Args args,
			@Local(argsOnly = true) Entity entity,
			@Local(ordinal = 1, argsOnly = true) float tickDelta,
			@Local(argsOnly = true) MultiBufferSource bufferSource
	) {
		if (entity.pc$disintegrating()) {
			args.set(2, 0f);
			// this allocates, but it's probably not a problem unless there's like 1000+ disintegrating entities
			args.set(4, new MultiBufferSource() {
				private final float ticks = entity.pc$disintegrateTicks() + tickDelta;

				@NotNull
				@Override
				public VertexConsumer getBuffer(RenderType renderType) {
					// this won't work with non-translucent render types unless we use some sort of mapping, but there doesn't seem to be a good way to make a conversion map for entity render types
					return new DisintegrationVertexConsumer(bufferSource.getBuffer(renderType), ticks);
				}
			});
		}
	}
}
