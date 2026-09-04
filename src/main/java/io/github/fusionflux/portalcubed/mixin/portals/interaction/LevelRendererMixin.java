package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import io.github.fusionflux.portalcubed.framework.shape.Line;
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow
	@Final
	private GameRenderer gameRenderer;

	@Inject(method = "submitBlockOutline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.AFTER))
	private void submitBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci,
	                                @Local(name = "state") BlockOutlineRenderState state, @Local(name = "outlineColor") int outlineColor, @Local(name = "blockOutlineRenderType") RenderType blockOutlineRenderType) {
		RaycastResult.Portal selectedPortal = Minecraft.getInstance().selectedPortal();
		if (selectedPortal == null)
			return;

		Portal portal = selectedPortal.portal.get();

		Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
		poseStack.pushPose();
		poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		if (state.highContrast()) {
			submitOutline(poseStack, submitNodeCollector, RenderTypes.secondaryBlockOutline(), portal, -16777216, 7.0F, state.isTranslucent());
		}

		submitOutline(
				poseStack,
				submitNodeCollector,
				blockOutlineRenderType,
				portal,
				outlineColor,
				this.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth,
				state.isTranslucent()
		);

		poseStack.popPose();
	}

	@Unique
	private static void submitOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, Portal portal, int color, float width, boolean afterTerrain) {
		CustomFeatureRenderer.Submit submit = new CustomFeatureRenderer.Submit(poseStack.last().copy(), renderType, (pose, buffer) -> {
			Vector3f normal = new Vector3f();
			for (Line line : portal.quad.lines()) {
				double x1 = line.from().x;
				double y1 = line.from().y;
				double z1 = line.from().z;
				double x2 = line.to().x;
				double y2 = line.to().y;
				double z2 = line.to().z;

				// Taken from ShapeOutlineFeatureRenderer
				normal.set((float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1)).normalize();
				buffer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(color).setNormal(pose, normal).setLineWidth(width);
				buffer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(color).setNormal(pose, normal).setLineWidth(width);
			}
		});
		submitNodeCollector.submitCustom(afterTerrain ? SubmitRenderPhases.AFTER_TERRAIN : SubmitRenderPhases.SHAPE_OUTLINES, submit);
	}
}
