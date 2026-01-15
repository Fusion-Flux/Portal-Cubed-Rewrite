package io.github.fusionflux.portalcubed.content.portal.graphics.render;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.PortalLookup;
import io.github.fusionflux.portalcubed.content.portal.sync.EntityState;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
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
	private final RenderBuffers renderBuffers;
	private final EntityRenderDispatcher entityRenderDispatcher;

	private ClientLevel world;
	private final List<CrossPortalEntity> entities = new ReferenceArrayList<>();

	public CrossPortalEntityRenderer(RenderBuffers renderBuffers, EntityRenderDispatcher entityRenderDispatcher) {
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
		PortalLookup portalLookup = this.world.portalManager().lookup();
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

				PortalHitResult hit = portalLookup.clip(start, end, 1);
				if (hit instanceof PortalHitResult.Tail tail) {
					this.entities.add(new CrossPortalEntity(entity, tickDelta, position, tail));

					break;
				}
			}
		}
	}

	public void withClippingPlane(Vec3 camPos, Portal portal, Runnable runnable) {
		portal.plane.getClipping(RenderSystem.getModelViewStack(), camPos, ShaderPatcher.CLIPPING_PLANES[1]);
		GL11.glEnable(GL11.GL_CLIP_PLANE1);

		runnable.run();

		GL11.glDisable(GL11.GL_CLIP_PLANE1);
	}

	public void render(PoseStack matrices, Camera camera) {
		Vec3 camPos = camera.getPosition();
		SimpleBufferSource bufferSource = ((RenderBuffersExt) this.renderBuffers).pc$crossPortalBufferSource();
		for (CrossPortalEntity crossPortalEntity : this.entities) {
			if (crossPortalEntity.shouldBeSkipped())
				continue;

			Portal inPortal = crossPortalEntity.inPortal.get();
			Portal outPortal = crossPortalEntity.outPortal;
			Vec3 transformedPos = crossPortalEntity.transformedPos();
			Vec3 transformedViewPos = transformedPos.subtract(camPos);
			Entity entity = crossPortalEntity.entity;
			float tickDelta = crossPortalEntity.tickDelta;

			matrices.pushPose();

			matrices.translate(transformedViewPos.x, transformedViewPos.y, transformedViewPos.z);
			matrices.mulPose(inPortal.rotation().conjugate(new Quaternionf()).premul(outPortal.rotation180));
			matrices.translate(-transformedViewPos.x, -transformedViewPos.y, -transformedViewPos.z);

			this.withClippingPlane(camPos, outPortal, () -> {
				this.entityRenderDispatcher
						.render(entity, transformedViewPos.x, transformedViewPos.y, transformedViewPos.z, tickDelta, matrices, bufferSource, this.entityRenderDispatcher.getPackedLightCoords(entity, tickDelta));
				bufferSource.flush();
			});

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

	public record CrossPortalEntity(Entity entity, float tickDelta, Vec3 position, PortalReference inPortal, Portal outPortal, PortalTransform transform) {
		public CrossPortalEntity(Entity entity, float tickDelta, Vec3 position, PortalHitResult.Tail hit) {
			this(entity, tickDelta, position, hit.enteredPortal(), hit.exitedPortal().get(), PortalTransform.of(hit));
		}

		public boolean shouldBeSkipped() {
			Minecraft mc = Minecraft.getInstance();
			if (this.entity != mc.player || !mc.options.getCameraType().isFirstPerson())
				return false;

			return PortalRenderer.recursion() == 1 && this.inPortal.id.equals(PortalRenderer.getRenderingPortal());
		}

		public Vec3 transformedPos() {
			return this.transform.applyAbsolute(this.position);
		}
	}
}
