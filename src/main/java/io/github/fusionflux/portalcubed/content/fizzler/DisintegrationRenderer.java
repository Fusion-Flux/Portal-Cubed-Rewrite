package io.github.fusionflux.portalcubed.content.fizzler;

import java.util.Set;
import java.util.function.Consumer;

import org.joml.Vector4f;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.extension.DisintegrationExt;
import io.github.fusionflux.portalcubed.framework.extension.RenderBuffersExt;
import io.github.fusionflux.portalcubed.framework.render.SimpleBufferSource;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import io.github.fusionflux.portalcubed.mixin.client.LevelRendererAccessor;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class DisintegrationRenderer {
	public static final Vector4f DISINTEGRATION_COLOR_MODIFIER = new Vector4f(1f);
	public static final float DARKEN = 0.15f;
	public static final float TRANSLUCENCY_START_PROGRESS = (DisintegrationExt.DISINTEGRATE_TICKS - DisintegrationExt.TRANSLUCENCY_START_TICKS) / (float) DisintegrationExt.DISINTEGRATE_TICKS;
	public static final Set<String> DONT_DARKEN_RENDER_TYPES = ImmutableSet.of("eyes", "entity_translucent_emissive", "beacon_beam", PortalCubed.id("emissive").toString());

	public static final ResourceLocation FLASH_TEXTURE = PortalCubed.id("textures/misc/fizzle_flash.png");
	public static final float FLASH_SIZE = 24f/16f;
	public static final float FLASH_SPEED = 0.7f;
	public static final float MIN_FLASH_ALPHA = 0.4f;

	public static void renderFlash(Entity entity, PoseStack matrices, float tickDelta, MultiBufferSource vertexConsumers) {
		float ticks = entity.pc$disintegrateTicks() + tickDelta;
		if (entity.getType().is(PortalCubedEntityTags.FIZZLES_WITHOUT_FLASH) || ticks <= DisintegrationExt.TRANSLUCENCY_START_TICKS)
			return;

		matrices.pushPose();
		matrices.translate(0, entity.getBoundingBox().getYsize() / 2, 0);
		matrices.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
		matrices.mulPose(Axis.YP.rotationDegrees(180));
		matrices.mulPose(Axis.XP.rotationDegrees(270));
		matrices.scale(FLASH_SIZE, FLASH_SIZE, FLASH_SIZE);
		matrices.translate(-.5, 0, -.5);
		RenderingUtils.renderQuad(matrices, vertexConsumers.getBuffer(RenderType.beaconBeam(FLASH_TEXTURE, true)), LightTexture.FULL_BRIGHT, getFlashColor(ticks));
		matrices.popPose();
	}

	private static int getFlashColor(float ticks) {
		float value = ticks * FLASH_SPEED;
		float alpha = Mth.clamp(Mth.sin(value * 3) + Mth.cos(value * 2), MIN_FLASH_ALPHA, 1f);
		return ColorABGR.withAlpha(0xFFFFFF, alpha);
	}

	public static void wrapRender(float ticks, Consumer<MultiBufferSource> renderer) {
		RenderBuffersExt renderBuffers = (RenderBuffersExt) ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getRenderBuffers();
		BufferSource bufferSource = renderBuffers.pc$disintegratingBufferSource();
		BufferSource emissiveBufferSource = renderBuffers.pc$disintegratingEmissiveBufferSource();
		renderer.accept(renderType -> (DONT_DARKEN_RENDER_TYPES.contains(renderType.name) ? emissiveBufferSource : bufferSource).getBuffer(renderType));

		float progress = 1 - Math.min(ticks / DisintegrationExt.DISINTEGRATE_TICKS, 1);
		float alpha = 1 - Math.min((Math.max(0, progress - TRANSLUCENCY_START_PROGRESS) / (1 - TRANSLUCENCY_START_PROGRESS)) * 3, 1);
		float darken = Mth.lerp(Math.min(progress * (1 + TRANSLUCENCY_START_PROGRESS), 1), 1f, DARKEN);

		DISINTEGRATION_COLOR_MODIFIER.set(darken, darken, darken, alpha);
		bufferSource.flush();

		DISINTEGRATION_COLOR_MODIFIER.set(1f, 1f, 1f, alpha);
		emissiveBufferSource.flush();

		DISINTEGRATION_COLOR_MODIFIER.set(1f);
	}

	public static final class BufferSource extends SimpleBufferSource {
		@Override
		protected void setupRenderState(RenderType type) {
			super.setupRenderState(type);
			RenderStateShard.TRANSLUCENT_TRANSPARENCY.setupRenderState();
		}
	}
}
