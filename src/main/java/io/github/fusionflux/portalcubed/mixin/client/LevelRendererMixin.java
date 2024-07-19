package io.github.fusionflux.portalcubed.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.fusionflux.portalcubed.content.portal.PortalRenderer;

import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@WrapOperation(
		method = "renderLevel",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
		)
	)
	private void renderMultiBlockBreakingTexture(BlockRenderDispatcher instance, BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack matrices, VertexConsumer vertexConsumer, Operation<Void> original) {
		if (state.getBlock() instanceof AbstractMultiBlock multiBlock) {
			for (var quadrantPos : multiBlock.quadrantIterator(multiBlock.getOriginPos(pos, state), state)) {
				matrices.pushPose();
				matrices.translate(quadrantPos.getX() - pos.getX(),  quadrantPos.getY() - pos.getY(), quadrantPos.getZ() - pos.getZ());
				original.call(instance, world.getBlockState(quadrantPos), quadrantPos, world, matrices, vertexConsumer);
				matrices.popPose();
			}
		} else {
			original.call(instance, state, pos, world, matrices, vertexConsumer);
		}
	}

	@WrapOperation(method = "renderLevel",  at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", remap = false))
	private void replaceClearingIfRenderingPortal(int mask, boolean checkError, Operation<Void> original) {
		if (PortalRenderer.isRenderingView()) {
			// Setup state
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
			GL11.glDepthRange(1, 1);

			RenderingUtils.renderFullScreenQuad(RenderingUtils.CLEAR_COLOR);

			// Cleanup state
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			GL11.glDepthRange(0, 1);
		} else {
			original.call(mask, checkError);
		}
	}
}
