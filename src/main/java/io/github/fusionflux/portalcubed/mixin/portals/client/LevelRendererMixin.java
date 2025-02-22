package io.github.fusionflux.portalcubed.mixin.portals.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.portal.renderer.CrossPortalEntityRenderer;
import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import io.github.fusionflux.portalcubed.content.portal.sync.EntityState;
import io.github.fusionflux.portalcubed.framework.extension.RenderBuffersExt;
import io.github.fusionflux.portalcubed.framework.render.SimpleBufferSource;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow
	private RenderBuffers renderBuffers;

	@Unique
	private CrossPortalEntityRenderer crossPortalEntityRenderer;

	@WrapOperation(
			method = "method_62218",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(I)V",
					remap = false
			)
	)
	private static void replaceClearingIfRenderingPortal(int mask, Operation<Void> original, @Local(argsOnly = true) Vector4f clearColor) {
		if (PortalRenderer.isRenderingView()) {
			// Setup state
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
			GL11.glDepthRange(1, 1);
			GL11.glDisable(GL11.GL_CLIP_PLANE0);

			RenderingUtils.renderFullScreenQuad(clearColor.x, clearColor.y, clearColor.z);

			// Cleanup state
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			GL11.glDepthRange(0, 1);
			GL11.glEnable(GL11.GL_CLIP_PLANE0);
		} else {
			original.call(mask);
		}
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initCrossPortalEntityRenderer(
			Minecraft minecraft, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, RenderBuffers renderBuffers, CallbackInfo ci) {
		this.crossPortalEntityRenderer = new CrossPortalEntityRenderer(minecraft, renderBuffers, entityRenderDispatcher);
	}

	@Inject(method = "setLevel", at = @At("HEAD"))
	private void setCrossPortalEntityRendererWorld(@Nullable ClientLevel world, CallbackInfo ci) {
		this.crossPortalEntityRenderer.setWorld(world);
	}

	@Inject(method = "renderLevel", at = @At("TAIL"))
	private void clearCrossPortalEntities(CallbackInfo ci) {
		this.crossPortalEntityRenderer.clear();
	}

	@Inject(
			method = "renderLevel",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/LevelRenderer;collectVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Ljava/util/List;)Z"
			)
	)
	private void collectCrossPortalEntities(CallbackInfo ci, @Local(argsOnly = true) Camera camera, @Local Frustum frustum, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		this.crossPortalEntityRenderer.collectEntities(frustum, deltaTracker);
	}

	@Inject(method = "renderEntities", at = @At("HEAD"))
	private void renderCrossPortalEntities(PoseStack matrices, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> entities, CallbackInfo ci) {
		this.crossPortalEntityRenderer.render(matrices, camera);
	}

	@WrapOperation(
			method = "renderEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
			)
	)
	private void applyClippingPlaneForPortalIntersectingEntity(EntityRenderDispatcher instance, Entity entity, double xOffset, double yOffset, double zOffset, float partialTick,
															   PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Operation<Void> original,
															   @Local(ordinal = 0, argsOnly = true) double camX, @Local(ordinal = 1, argsOnly = true) double camY,
															   @Local(ordinal = 2, argsOnly = true) double camZ) {
		CrossPortalEntityRenderer.CrossPortalEntity crossPortalEntity = this.crossPortalEntityRenderer.getCrossPortalEntity(entity);
		if (crossPortalEntity != null) {
			SimpleBufferSource crossPortalBufferSource = ((RenderBuffersExt) this.renderBuffers).pc$crossPortalBufferSource();
			this.crossPortalEntityRenderer.withClippingPlane(new Vec3(camX, camY, camZ), crossPortalEntity.inPortal(), () -> {
				original.call(instance, entity, xOffset, yOffset, zOffset, partialTick, poseStack, crossPortalBufferSource, packedLight);
				crossPortalBufferSource.flush();
			});
		} else {
			original.call(instance, entity, xOffset, yOffset, zOffset, partialTick, poseStack, bufferSource, packedLight);
		}
	}

	@Inject(
			method = "renderEntity",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/renderer/LevelRenderer;entityRenderDispatcher:Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;",
					ordinal = 0
			)
	)
	private void applyMidTeleportSubTickMotion(Entity entity, double camX, double camY, double camZ, float partialTicks,
											   PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci,
											   @Local(ordinal = 3) LocalDoubleRef x, @Local(ordinal = 4) LocalDoubleRef y,
											   @Local(ordinal = 5) LocalDoubleRef z) {
		EntityState override = entity.getTeleportProgressTracker().getEntityStateOverride(partialTicks);
		if (override != null) {
			x.set(override.pos().x);
			y.set(override.pos().y);
			z.set(override.pos().z);
		}
	}
}
