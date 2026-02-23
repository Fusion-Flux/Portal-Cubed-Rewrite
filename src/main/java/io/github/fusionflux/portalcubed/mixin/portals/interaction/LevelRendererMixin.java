package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import io.github.fusionflux.portalcubed.framework.shape.Quad;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "renderBlockOutline", at = @At("RETURN"))
	private void renderPortalOutline(Camera camera, MultiBufferSource.BufferSource buffers, PoseStack transforms, boolean sort, CallbackInfo ci) {
		if (sort) {
			// this method is called twice, once with sort = false and once with sort = true
			return;
		}

		RaycastResult.Portal selectedPortal = this.minecraft.selectedPortal();
		if (selectedPortal == null)
			return;

		Portal portal = selectedPortal.portal.get();
		boolean highContrast = this.minecraft.options.highContrastBlockOutline().get();

		Vec3 cameraPos = camera.getPosition();
		transforms.pushPose();
		transforms.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		if (highContrast) {
			VertexConsumer secondaryBuffer = buffers.getBuffer(RenderType.secondaryBlockOutline());
			renderOutline(transforms, secondaryBuffer, portal, -16777216);
		}

		VertexConsumer buffer = buffers.getBuffer(RenderType.lines());
		int color = highContrast ? -11010079 : ARGB.color(102, -16777216);
		renderOutline(transforms, buffer, portal, color);

		buffers.endLastBatch();
		transforms.popPose();
	}

	@Unique
	private static void renderOutline(PoseStack transforms, VertexConsumer buffer, Portal portal, int color) {
		Quad quad = portal.quad;
		RenderingUtils.renderLine(transforms, buffer, quad.topLeft(), quad.bottomLeft(), color);
		RenderingUtils.renderLine(transforms, buffer, quad.bottomLeft(), quad.bottomRight(), color);
		RenderingUtils.renderLine(transforms, buffer, quad.bottomRight(), quad.topRight(), color);
		RenderingUtils.renderLine(transforms, buffer, quad.topRight(), quad.topLeft(), color);
	}
}
