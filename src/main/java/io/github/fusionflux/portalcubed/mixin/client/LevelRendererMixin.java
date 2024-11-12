package io.github.fusionflux.portalcubed.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import com.llamalad7.mixinextras.sugar.Share;

import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.TeleportStep;
import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;

import io.github.fusionflux.portalcubed.framework.util.RangeSequence;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(method = "renderEntity", at = @At("HEAD"))
	private void replaceValuesPostTeleport(Entity entity, double cameraX, double cameraY, double cameraZ,
										   float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers,
										   CallbackInfo ci,
										   @Share("pos") LocalRef<Vec3> pos, @Share("yaw") LocalRef<Float> yaw) {

		RangeSequence<TeleportStep> steps = entity.getPortalTeleport();
		if (steps != null) {
			RangeSequence.Entry<TeleportStep> entry = steps.getEntry(tickDelta);
			float range = entry.max() - entry.min();
			float localProgress = (tickDelta - entry.min()) / range;
			TeleportStep step = entry.value();
			Vec3 centerToPos = PortalTeleportHandler.getCenterToPosOffset(entity);
			pos.set(step.pos(localProgress).add(centerToPos));
			yaw.set(step.rotations().getY());
		}
	}

	@WrapOperation(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 0))
	private double setX(double delta, double start, double end, Operation<Double> original, @Share("pos") LocalRef<Vec3> pos) {
		return pos.get() != null ? pos.get().x : original.call(delta, start, end);
	}

	@WrapOperation(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 1))
	private double setY(double delta, double start, double end, Operation<Double> original, @Share("pos") LocalRef<Vec3> pos) {
		return pos.get() != null ? pos.get().y : original.call(delta, start, end);
	}

	@WrapOperation(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 2))
	private double setZ(double delta, double start, double end, Operation<Double> original, @Share("pos") LocalRef<Vec3> pos) {
		return pos.get() != null ? pos.get().z : original.call(delta, start, end);
	}

	@WrapOperation(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
	private float setYaw(float delta, float start, float end, Operation<Float> original, @Share("yaw") LocalRef<Float> yaw) {
		return yaw.get() != null ? yaw.get() : original.call(delta, start, end);
	}

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
