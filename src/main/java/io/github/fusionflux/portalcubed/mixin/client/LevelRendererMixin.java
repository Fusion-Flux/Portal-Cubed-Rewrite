package io.github.fusionflux.portalcubed.mixin.client;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@WrapOperation(
		method = "renderBlockDestroyAnimation",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
		)
	)
	private void renderMultiBlockBreakingTexture(BlockRenderDispatcher instance, BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack matrices, VertexConsumer vertexConsumer, Operation<Void> original) {
		if (state.getBlock() instanceof AbstractMultiBlock multiBlock) {
			for (BlockPos quadrantPos : multiBlock.quadrants(multiBlock.getOriginPos(pos, state), state)) {
				matrices.pushPose();
				matrices.translate(quadrantPos.getX() - pos.getX(),  quadrantPos.getY() - pos.getY(), quadrantPos.getZ() - pos.getZ());
				original.call(instance, world.getBlockState(quadrantPos), quadrantPos, world, matrices, vertexConsumer);
				matrices.popPose();
			}
		} else {
			original.call(instance, state, pos, world, matrices, vertexConsumer);
		}
	}

	@WrapOperation(method = "method_62218",  at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(I)V", remap = false))
	private static void replaceClearingIfRenderingPortal(int mask, Operation<Void> original, @Local(argsOnly = true) Vector4f clearColor) {
		if (PortalRenderer.isRenderingView()) {
			// Setup state
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
			GL11.glDepthRange(1, 1);

			RenderingUtils.renderFullScreenQuad(clearColor.x, clearColor.y, clearColor.z);

			// Cleanup state
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			GL11.glDepthRange(0, 1);
		} else {
			original.call(mask);
		}
	}
}
