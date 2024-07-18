package io.github.fusionflux.portalcubed.content.portal;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.framework.shape.VoxelShenanigans;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import io.github.fusionflux.portalcubed.mixin.client.CameraAccessor;
import io.github.fusionflux.portalcubed.mixin.client.LevelRendererAccessor;
import io.github.fusionflux.portalcubed.mixin.client.RenderSystemAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.phys.shapes.VoxelShape;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.ARBDepthClamp;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public class PortalRenderer {
	public static final Color RED = new Color(1, 0, 0, 1);
	public static final Color GREEN = new Color(0.5f, 1, 0.5f, 1);
	public static final Color BLUE = new Color(0, 0, 1, 1);
	public static final Color ORANGE = new Color(1, 0.5f, 0, 1);
	public static final Color PURPLE = new Color(0.5f, 0, 1, 1);
	public static final Color CYAN = new Color(0, 1, 1, 1);

	public static final Color PLANE_COLOR = new Color(1, 1, 1, 1);
	public static final Color ACTIVE_PLANE_COLOR = GREEN;

	public static final double OFFSET_FROM_WALL = 0.001;

	private static final int MAX_RECURSIONS = 10;
	private static final Deque<PoseStack> VIEW_MATRICES = Util.make(Queues.newArrayDeque(), stack -> {
		for (int i = 0; i < MAX_RECURSIONS; i++) {
			stack.add(new PoseStack());
		}
	});
	private static int recursion = 0;

	public static boolean isRenderingView() {
		return recursion > 0;
	}

	public static boolean shouldRenderView(PortalInstance portal, Camera camera) {
		return recursion < MAX_RECURSIONS && portal.plane.test(camera);
	}

	private static void render(WorldRenderContext context) {
		if (!(context.consumers() instanceof final MultiBufferSource.BufferSource vertexConsumers))
			return;
		ClientPortalManager manager = context.world().portalManager();
		Collection<PortalPair> pairs = manager.getAllPairs();
		if (pairs.isEmpty())
			return;

		PoseStack matrices = context.matrixStack();
		Vec3 camPos = context.camera().getPosition();
		Frustum frustum = Objects.requireNonNull(context.frustum());
		matrices.pushPose();
		boolean renderDebug = Minecraft.getInstance().getDebugOverlay().showDebugScreen();
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GlStateManager._enableDepthTest();
		for (PortalPair pair : pairs) {
			for (PortalInstance portal : pair) {
				if (frustum.isVisible(portal.renderBounds)) {
					renderPortal(pair, portal, matrices, vertexConsumers, context.camera(), context.worldRenderer(), context.tickDelta(), context.limitTime(), context.gameRenderer());
//					if (renderDebug) {
//						renderPortalDebug(portal, context, matrices, vertexConsumers);
//					}
				}
			}
		}

		matrices.popPose();
		vertexConsumers.endLastBatch();

		if (!isRenderingView()) {
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			GlStateManager._disableDepthTest();
			RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
		}
	}

	private static void renderPortal(PortalPair pair, PortalInstance portal, PoseStack matrices, MultiBufferSource vertexConsumers, Camera camera, LevelRenderer worldRenderer, float tickDelta, long limitTime, GameRenderer gameRenderer) {
		RenderType renderType = RenderType.beaconBeam(portal.data.settings().shape().texture, true);
		VertexConsumer vertices = vertexConsumers.getBuffer(renderType);
		matrices.pushPose();
		// translate to portal pos
		matrices.translate(portal.data.origin().x, portal.data.origin().y, portal.data.origin().z);
		// apply rotations
		matrices.mulPose(portal.rotation); // rotate towards facing direction
		matrices.mulPose(Axis.ZP.rotationDegrees(180));
		// slight offset so origin is center of portal
		matrices.translate(-0.5f, -1, 0);
		// small offset away from the wall to not z-fight
		matrices.translate(0, 0, -OFFSET_FROM_WALL);
		// scale quad - 32x32 texture, half is used. scale the 1x1 to a 2x2.
		matrices.scale(2, 2, 2);

		RenderingUtils.renderQuad(matrices, vertices, LightTexture.FULL_BRIGHT, portal.data.settings().color());

		PortalInstance linked = pair.other(portal);
		if (linked != null && shouldRenderView(portal, camera)) {
			// Draw stencil
			RenderSystem.depthMask(false);
			RenderingUtils.setupStencilForWriting(recursion, true);
			renderPortalStencil(portal, matrices);
			RenderSystem.depthMask(true);

			// Backup old state
			StateCapture oldState = StateCapture.capture();

			// Setup camera
			Vec3 camPos = PortalTeleportHandler.teleportAbsoluteVecBetween(camera.getPosition(), portal, linked);
			((CameraAccessor) camera).setPosition(camPos);

			Quaternionf cameraRotation = new Quaternionf().rotateYXZ((180 - camera.getYRot()) * Mth.DEG_TO_RAD, -camera.getXRot() * Mth.DEG_TO_RAD, 0);
			for (int i = 0; i <= recursion; i++) {
				cameraRotation.premul(portal.rotation.invert(new Quaternionf()));
				cameraRotation.premul(linked.rotation180);
			}
			cameraRotation.conjugate();

			PoseStack viewMatrix = VIEW_MATRICES.pop();
			viewMatrix.setIdentity();
			viewMatrix.mulPose(cameraRotation);
			PoseStack.Pose view = viewMatrix.last();

			RenderSystem.setInverseViewRotationMatrix(view.normal().invert(new Matrix3f()));
			linked.plane.clipProjection(view.pose(), camPos, RenderSystem.getProjectionMatrix());

			((LevelRendererAccessor) worldRenderer).callPrepareCullFrustum(viewMatrix, camPos, gameRenderer.getProjectionMatrix(Minecraft.getInstance().options.fov().get()));

			// Render the world
			recursion++;
			RenderingUtils.setupStencilToRenderIfValue(recursion);
			RenderSystem.stencilMask(0x00);
			RenderSystemAccessor.setModelViewStack(new PoseStack());
			worldRenderer.renderLevel(viewMatrix, tickDelta, limitTime, false, camera, gameRenderer, gameRenderer.lightTexture(), RenderSystem.getProjectionMatrix());
			recursion--;

			// Restore old state
			VIEW_MATRICES.push(viewMatrix);
			oldState.restore();

			// Restore depth
			RenderingUtils.setupStencilForWriting(recursion + 1, false);
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
			RenderSystem.colorMask(false, false, false, false);
			RenderingUtils.renderFullScreenQuad();
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			RenderSystem.colorMask(true, true, true, true);
		}

		matrices.popPose();
	}

	private static void renderPortalStencil(PortalInstance portal, PoseStack matrices) {
		BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();

		// Setup state
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, portal.data.settings().shape().stencilTexture);
		GL11.glEnable(ARBDepthClamp.GL_DEPTH_CLAMP);
		RenderSystem.colorMask(false, false, false, false);

		// Build quad
		Matrix4f matrix = matrices.last().pose();
		builder.vertex(matrix, 1, 1, 0).uv(1, 1).endVertex();
		builder.vertex(matrix, 1, 0, 0).uv(1, 0).endVertex();
		builder.vertex(matrix, 0, 0, 0).uv(0, 0).endVertex();
		builder.vertex(matrix, 0, 1, 0).uv(0, 1).endVertex();

		// Draw quad
		BufferUploader.drawWithShader(builder.end());

		// Cleanup state
		GL11.glDisable(ARBDepthClamp.GL_DEPTH_CLAMP);
		RenderSystem.colorMask(true, true, true, true);
	}

	public record StateCapture(
			PoseStack modelViewStack,
			Matrix4f modelViewMatrix,
			Matrix3f inverseViewRotationMatrix,
			Matrix4f projectionMatrix,
			Frustum frustum,
			Vec3 cameraPos,
			Vector3f[] shaderLightDirections
	) {
		public static StateCapture capture() {
			return new StateCapture(
					RenderSystem.getModelViewStack(),
					new Matrix4f(RenderSystem.getModelViewMatrix()),
					RenderSystem.getInverseViewRotationMatrix(),
					new Matrix4f(RenderSystem.getProjectionMatrix()),
					((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getCullingFrustum(),
					Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(),
					RenderSystemAccessor.getShaderLightDirections().clone()
			);
		}

		public void restore() {
			RenderSystemAccessor.setModelViewStack(this.modelViewStack);
			RenderSystemAccessor.setModelViewMatrix(this.modelViewMatrix);
			RenderSystem.setInverseViewRotationMatrix(this.inverseViewRotationMatrix);
			RenderSystemAccessor.setProjectionMatrix(this.projectionMatrix);
			((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).setCullingFrustum(this.frustum);
			((CameraAccessor) Minecraft.getInstance().gameRenderer.getMainCamera()).setPosition(this.cameraPos);
			RenderSystem.setShaderLights(shaderLightDirections[0], shaderLightDirections[1]);
			GlStateManager._enableDepthTest();
		}
	}

//	private static void renderPortalDebug(PortalInstance portal, WorldRenderContext ctx, PoseStack matrices, MultiBufferSource vertexConsumers) {
//		// render a box around the portal's plane
//		Color planeColor = portal.isActive() ? ACTIVE_PLANE_COLOR : PLANE_COLOR;
//		renderBox(matrices, vertexConsumers, portal.plane, planeColor);
//		// collision bounds
//		renderBox(matrices, vertexConsumers, portal.entityCollisionArea, RED);
//		renderBox(matrices, vertexConsumers, portal.collisionCollectionArea, PURPLE);
//		renderBox(matrices, vertexConsumers, portal.collisionModificationBox, CYAN);
//		// cross-portal collision
//		PortalInstance linked = portal.getLinked();
//		if (linked != null) {
//			renderCollision(ctx, portal, linked);
//		}
//		// render player's raycast through
//		Camera camera = ctx.camera();
//		Vec3 pos = camera.getPosition();
//		Vector3f lookVector = camera.getLookVector().normalize(3, new Vector3f());
//		Vec3 end = pos.add(lookVector.x, lookVector.y, lookVector.z);
//		PortalHitResult hit = ctx.world().portalManager().clipPortal(pos, end);
//		if (hit != null) {
//			// start -> hitIn
//			RenderingUtils.renderLine(matrices, vertexConsumers, hit.start(), hit.hitIn(), ORANGE);
//			// box at hitIn
//			AABB hitInBox = AABB.ofSize(hit.hitIn(), 0.1, 0.1, 0.1);
//			renderBox(matrices, vertexConsumers, hitInBox, ORANGE);
//			// box at hitOut
//			AABB hitOutBox = AABB.ofSize(hit.hitOut(), 0.1, 0.1, 0.1);
//			renderBox(matrices, vertexConsumers, hitOutBox, BLUE);
//			// hitOut -> end
//			RenderingUtils.renderLine(matrices, vertexConsumers, hit.hitOut(), hit.teleportedEnd(), BLUE);
//			// box at end
//			AABB endBox = AABB.ofSize(hit.teleportedEnd(), 0.1, 0.1, 0.1);
//			renderBox(matrices, vertexConsumers, endBox, GREEN);
//		}
//	}

	private static void renderCollision(WorldRenderContext ctx, PortalInstance portal, PortalInstance linked) {
		Camera camera = ctx.camera();
		Entity entity = camera.getEntity();
		ClientLevel level = ctx.world();
		PoseStack matrices = ctx.matrixStack();
		VertexConsumer vertices = Objects.requireNonNull(ctx.consumers()).getBuffer(RenderType.lines());

		List<VoxelShape> shapes = VoxelShenanigans.getShapesBehindPortal(level, entity, portal, linked);
		shapes.forEach(shape -> LevelRenderer.renderVoxelShape(
				matrices, vertices, shape,
				0, 0, 0,
				1, 1, 1, 1,
				true
		));
	}

	public static void init() {
		WorldRenderEvents.END.register(PortalRenderer::render);
	}
}
