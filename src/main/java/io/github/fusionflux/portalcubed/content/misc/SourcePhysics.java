package io.github.fusionflux.portalcubed.content.misc;

import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import io.github.fusionflux.portalcubed.mixin.EntityAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.quiltmc.loader.api.minecraft.ClientOnly;

/*
 * Source-like physics:
 * - no air drag
 * - soft air speed limit of 30u/s (about half a block)
 * - speed limit is full vector length, not components
 * - input is ignored when the projection of velocity onto acceleration is above the limit
 * Reference: https://steamcommunity.com/sharedfiles/filedetails/?id=184184420
 */
public class SourcePhysics {
	// // 128 in a 2x2 panel
	public static final double BLOCKS_PER_UNIT = 1 / 64f;
	public static final double SPEED_LIMIT_UNITS = 30;
	public static final double SPEED_LIMIT_BLOCKS = BLOCKS_PER_UNIT * SPEED_LIMIT_UNITS;
	public static final double SPEED_LIMIT = SPEED_LIMIT_BLOCKS / 20; // 20 tps

	public static boolean appliesTo(Player player) {
		if (!player.getItemBySlot(EquipmentSlot.FEET).is(PortalCubedItemTags.APPLY_SOURCE_PHYSICS))
			return false;

		if (player.onGround() || player.getAbilities().flying)
			return false;

		if (player.getPose() != Pose.STANDING && player.getPose() != Pose.CROUCHING)
			return false;

		if (player.onClimbable())
			return false;

		return !player.isInLiquid();
	}

	public static float getAirDrag(LivingEntity entity, float original) {
		return entity instanceof Player player && appliesTo(player) ? 1 : original;
	}

	@ClientOnly
	public static void applyInput(LocalPlayer player) {
		if (!appliesTo(player))
			return;

		Vec3 vel = player.getDeltaMovement();
		Vec3 accel = getAcceleration(player);

		double dot = vel.dot(accel);
		if (dot < 0) {
			// do nothing when input is pointing backwards
			return;
		}

		double projection = projectionMagnitude(vel, accel);
		System.out.println(projection);
		if (projection > SPEED_LIMIT) {
			// too fast, discard
			// don't use 0, will stop sprinting
			player.input.leftImpulse = 1E-4f;
			player.input.forwardImpulse = 1E-4f;
		}
	}

	public static double projectionMagnitude(Vec3 a, Vec3 b) {
		double angle = angleBetween(a, b);
		return a.length() * Math.cos(angle);
	}

	public static double angleBetween(Vec3 a, Vec3 b) {
		return a.dot(b) / (a.length() * b.length());
	}

	@ClientOnly
	public static Vec3 getAcceleration(LocalPlayer player) {
		Vec3 inputVec = new Vec3(player.input.leftImpulse, 0, player.input.forwardImpulse);
		return EntityAccessor.callGetInputVector(inputVec, 0.02f, player.getYRot());
	}

	public static class DebugRenderer implements HudRenderCallback {
		public static final Vec3i VEL_COLOR = new Vec3i(0, 125, 0);
		public static final Vec3i ACCEL_COLOR = new Vec3i(125, 0, 125);

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

			renderVec(player.getDeltaMovement().scale(100), VEL_COLOR);
			renderVec(getAcceleration(player).scale(1000), ACCEL_COLOR);

			RenderSystem.lineWidth(1);
			GlStateManager._enableCull();
			GlStateManager._depthMask(true);
		}

		private static void renderVec(Vec3 vec, Vec3i color) {
			Vector3f norm = vec.normalize().toVector3f();
			Tesselator tesselator = RenderSystem.renderThreadTesselator();
			BufferBuilder buffer = tesselator.getBuilder();
			// thicker outline
			RenderSystem.lineWidth(4);
			buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
			buffer.vertex(0, 0, 0).color(0, 0, 0, 255).normal(norm.x, norm.y, norm.z).endVertex();
			buffer.vertex(vec.x, vec.y, vec.z).color(0, 0, 0, 255).normal(norm.x, norm.y, norm.z).endVertex();
			tesselator.end();
			// main line
			RenderSystem.lineWidth(2);
			buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
			buffer.vertex(0, 0, 0).color(color.getX(), color.getY(), color.getZ(), 255).normal(norm.x, norm.y, norm.z).endVertex();
			buffer.vertex(vec.x, vec.y, vec.z).color(color.getX(), color.getY(), color.getZ(), 255).normal(norm.x, norm.y, norm.z).endVertex();
			tesselator.end();
		}
	}
}
