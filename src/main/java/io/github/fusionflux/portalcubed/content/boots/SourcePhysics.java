package io.github.fusionflux.portalcubed.content.boots;

import org.joml.Vector3f;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import io.github.fusionflux.portalcubed.mixin.EntityAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/*
Source-like physics:
- no air drag
- soft air speed limit of 30u/s (about half a block)
- speed limit is full vector length, not components
- input is ignored when the projection of velocity onto acceleration is above the limit
Reference: https://steamcommunity.com/sharedfiles/filedetails/?id=184184420
*/
public class SourcePhysics {
	// 128 in a 2x2 panel
	public static final double BLOCKS_PER_UNIT = 1 / 64f;
	public static final double SPEED_LIMIT_UNITS = 30;
	public static final double SPEED_LIMIT_BLOCKS = BLOCKS_PER_UNIT * SPEED_LIMIT_UNITS;
	public static final double SPEED_LIMIT = SPEED_LIMIT_BLOCKS / 20; // 20 tps

	public static boolean appliesTo(Player player) {
		if (!player.getItemBySlot(EquipmentSlot.FEET).is(PortalCubedItemTags.APPLY_SOURCE_PHYSICS))
			return false;

		if (player.getAbilities().flying)
			return false;

		if (player.getPose() != Pose.STANDING && player.getPose() != Pose.CROUCHING)
			return false;

		if (player.onClimbable())
			return false;

		return !player.isInLiquid();
	}

	@ClientOnly
	public static void applyInput(LocalPlayer player) {
		if (!appliesTo(player) || player.onGround())
			return;

		Vec3 vel = player.getDeltaMovement();
		Vec3 accel = getAcceleration(player);

		// do nothing when input is pointing backwards or perpendicular
		double dot = vel.normalize().dot(accel.normalize());
		if (dot < 0.1)
			return;

		Vec3 projection = projection(vel, accel);
		if (projection.length() > SPEED_LIMIT) {
			// too fast, discard
			// don't use 0, will stop sprinting
			player.input.leftImpulse = 1E-4f;
			player.input.forwardImpulse = 1E-4f;
		}
	}

	public static Vec3 projection(Vec3 a, Vec3 b) {
		double length = a.dot(b) / (b.length() * b.length());
		return b.scale(length);
	}

	@ClientOnly
	public static Vec3 getAcceleration(LocalPlayer player) {
		Vec3 inputVec = new Vec3(player.input.leftImpulse, 0, player.input.forwardImpulse);
		return EntityAccessor.callGetInputVector(inputVec, 0.02f, player.getYRot());
	}

	public static class DebugRenderer implements HudRenderCallback {
		public static final int SCALE = 500;
		public static final Vec3i VEL_COLOR = new Vec3i(0, 125, 0);
		public static final Vec3i ACCEL_COLOR = new Vec3i(125, 0, 125);
		public static final Vec3i PROJECTION_COLOR = new Vec3i(255,128,0);
		public static final Vec3i LIMIT_COLOR = new Vec3i(255, 0, 0);

		public static final DebugRenderer INSTANCE = new DebugRenderer();

		/** {@link Gui#renderCrosshair(GuiGraphics)} */
		@SuppressWarnings("JavadocReference")
		@Override
		public void onHudRender(GuiGraphics graphics, float tickDelta) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			if (player == null)
				return;

			if (!mc.gui.getDebugOverlay().showDebugScreen())
				return;

			Camera camera = mc.gameRenderer.getMainCamera();
			int width = mc.getWindow().getGuiScaledWidth();
			int height = mc.getWindow().getGuiScaledHeight();

			RenderSystem.enableBlend();
			PoseStack poseStack = RenderSystem.getModelViewStack();
			poseStack.pushPose();
			poseStack.mulPoseMatrix(graphics.pose().last().pose());
			poseStack.translate(width / 2f, height / 2f, 0);
			poseStack.mulPose(Axis.XN.rotationDegrees(camera.getXRot()));
			poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
			poseStack.scale(-1, -1, -1);
			RenderSystem.applyModelViewMatrix();
			doRender(player);
			poseStack.popPose();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.disableBlend();
		}

		/** {@link GLX#_renderCrosshair(int, boolean, boolean, boolean)} */
		private static void doRender(LocalPlayer player) {
			RenderSystem.assertOnRenderThread();
			GlStateManager._depthMask(false);
			GlStateManager._disableCull();
			RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);

			Vec3 vel = player.getDeltaMovement();
			renderVec(vel.scale(SCALE), VEL_COLOR);

			Vec3 limit = vel.normalize().scale(SPEED_LIMIT * SCALE);
			Vec3 limitEnd = limit.add(limit.normalize());
			renderLine(limit, limitEnd, LIMIT_COLOR);

			Vec3 accel = getAcceleration(player);
			if (accel.length() > 0) {
				Vec3 projection = projection(vel, accel).scale(SCALE);
				renderVec(projection, PROJECTION_COLOR);
				renderVec(accel.scale(SCALE), ACCEL_COLOR);
			}

			RenderSystem.lineWidth(1);
			GlStateManager._enableCull();
			GlStateManager._depthMask(true);
		}

		private static void renderVec(Vec3 vec, Vec3i color) {
			renderLine(Vec3.ZERO, vec, color);
		}

		private static void renderLine(Vec3 from, Vec3 to, Vec3i color) {
			Vec3 between = from.vectorTo(to);
			Vector3f norm = between.normalize().toVector3f();
			Tesselator tesselator = RenderSystem.renderThreadTesselator();
			BufferBuilder buffer = tesselator.getBuilder();
			// thicker outline
			RenderSystem.lineWidth(4);
			buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
			buffer.vertex(from.x, from.y, from.z).color(0, 0, 0, 255).normal(norm.x, norm.y, norm.z).endVertex();
			buffer.vertex(to.x, to.y, to.z).color(0, 0, 0, 255).normal(norm.x, norm.y, norm.z).endVertex();
			tesselator.end();
			// main line
			RenderSystem.lineWidth(2);
			buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
			buffer.vertex(from.x, from.y, from.z).color(color.getX(), color.getY(), color.getZ(), 255).normal(norm.x, norm.y, norm.z).endVertex();
			buffer.vertex(to.x, to.y, to.z).color(color.getX(), color.getY(), color.getZ(), 255).normal(norm.x, norm.y, norm.z).endVertex();
			tesselator.end();
		}
	}
}
