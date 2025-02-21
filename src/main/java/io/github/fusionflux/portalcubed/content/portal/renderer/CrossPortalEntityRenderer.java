package io.github.fusionflux.portalcubed.content.portal.renderer;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.ActivePortalLookup;
import io.github.fusionflux.portalcubed.content.portal.sync.EntityState;
import io.github.fusionflux.portalcubed.framework.extension.RenderBuffersExt;
import io.github.fusionflux.portalcubed.framework.render.SimpleBufferSource;
import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CrossPortalEntityRenderer {
	private final Minecraft minecraft;
	private final RenderBuffers renderBuffers;
	private final EntityRenderDispatcher entityRenderDispatcher;

	private ClientLevel world;
	private final List<CrossPortalEntity> entities = new ReferenceArrayList<>();

	public CrossPortalEntityRenderer(Minecraft minecraft, RenderBuffers renderBuffers, EntityRenderDispatcher entityRenderDispatcher) {
		this.minecraft = minecraft;
		this.renderBuffers = renderBuffers;
		this.entityRenderDispatcher = entityRenderDispatcher;
	}

	public void setWorld(@Nullable ClientLevel world) {
		this.world = world;
	}

	public void clear() {
		this.entities.clear();
	}

	public void collectEntities(Frustum frustum, DeltaTracker deltaTracker) {
		ActivePortalLookup portalLookup = this.world.portalManager().activePortals();
		for (Entity entity : this.world.entitiesForRendering()) {
			TickRateManager tickRateManager = this.world.tickRateManager();
			float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));

			EntityState override = entity.getTeleportProgressTracker().getEntityStateOverride(tickDelta);
			Vec3 position = override != null ? override.pos() : new Vec3(
					Mth.lerp(tickDelta, entity.xOld, entity.getX()),
					Mth.lerp(tickDelta, entity.yOld, entity.getY()),
					Mth.lerp(tickDelta, entity.zOld, entity.getZ())
			);
			Vec3 center = position.add(entity.position().vectorTo(PortalTeleportHandler.centerOf(entity)));

		    AABB boundingBox = entity.getBoundingBox();
			double xSize = boundingBox.getXsize() / 2;
			double ySize = boundingBox.getYsize() / 2;
			double zSize = boundingBox.getZsize() / 2;
			for (Direction face : Direction.values()) {
				Vec3 start = face.getUnitVec3().multiply(-xSize, -ySize, -zSize)
						.add(center);
				Vec3 end = face.getUnitVec3().multiply(xSize, ySize, zSize)
						.add(center);

				PortalHitResult hit = portalLookup.clip(start, end);
				if (hit != null) {
					if (PortalRenderer.isPortalVisible(frustum, hit.in()) || PortalRenderer.isPortalVisible(frustum, hit.out()))
						this.entities.add(new CrossPortalEntity(entity, tickDelta, position, hit.in(), hit.out()));
					break;
				}
			}
		}
	}

	public void withClippingPlane(Vec3 camPos, PortalInstance portal, Runnable runnable) {
		Vector4f oldClippingPlane = new Vector4f(ShaderPatcher.CLIPPING_PLANE);
		portal.plane.getClipping(RenderSystem.getModelViewStack(), camPos, ShaderPatcher.CLIPPING_PLANE);
		GL11.glEnable(GL11.GL_CLIP_PLANE0);

		runnable.run();

		ShaderPatcher.CLIPPING_PLANE.set(oldClippingPlane);
		if (!PortalRenderer.isRenderingView())
			GL11.glDisable(GL11.GL_CLIP_PLANE0);
	}

	public void render(PoseStack matrices, Camera camera) {
		Vec3 camPos = camera.getPosition();
		SimpleBufferSource bufferSource = ((RenderBuffersExt) this.renderBuffers).pc$crossPortalBufferSource();
		for (CrossPortalEntity crossPortalEntity : this.entities) {
			PortalInstance inPortal = crossPortalEntity.inPortal;
			PortalInstance outPortal = crossPortalEntity.outPortal;
			Vec3 transformedPos = PortalTeleportHandler.teleportAbsoluteVecBetween(crossPortalEntity.position, inPortal, outPortal);
			Vec3 transformedViewPos = transformedPos.subtract(camPos);

			Entity entity = crossPortalEntity.entity;
			float tickDelta = crossPortalEntity.tickDelta;
			if (entity == this.minecraft.player && this.minecraft.options.getCameraType().isFirstPerson()) {
				if (PortalRenderer.recursion() == 1 && PortalRenderer.getRenderingPortal() == inPortal)
					continue;
			}

			matrices.pushPose();

			matrices.translate(transformedViewPos.x, transformedViewPos.y, transformedViewPos.z);
			matrices.mulPose(inPortal.rotation().conjugate(new Quaternionf()).premul(outPortal.rotation180));
			matrices.translate(-transformedViewPos.x, -transformedViewPos.y, -transformedViewPos.z);

			this.entityRenderDispatcher
					.render(entity, transformedViewPos.x, transformedViewPos.y, transformedViewPos.z, tickDelta, matrices, bufferSource, this.entityRenderDispatcher.getPackedLightCoords(entity, tickDelta));
			this.withClippingPlane(camPos, outPortal, bufferSource::flush);

			matrices.popPose();
		}
	}

	@Nullable
	public CrossPortalEntity getCrossPortalEntity(Entity entity) {
		for (CrossPortalEntity crossPortalEntity : this.entities) {
			if (crossPortalEntity.entity == entity)
				return crossPortalEntity;
		}

		return null;
	}

	public record CrossPortalEntity(Entity entity, float tickDelta, Vec3 position, PortalInstance inPortal, PortalInstance outPortal) {
	}
}
