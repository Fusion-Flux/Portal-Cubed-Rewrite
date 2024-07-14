package io.github.fusionflux.portalcubed.content.fizzler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.extension.EntityExt;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorU8;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class DisintegrationRenderer {
	public static final ResourceLocation FLASH_TEXTURE = PortalCubed.id("textures/entity/fizzle_flash.png");
	public static final float FLASH_SIZE = 3f/4f;
	public static final float FLASH_SPEED = 0.85f;
	public static final float MIN_FLASH_ALPHA = 0.2f;

	public static void renderFlash(Entity entity, PoseStack matrices, float tickDelta, MultiBufferSource vertexConsumers) {
		float ticks = entity.pc$disintegrateTicks() + tickDelta;
		if (entity.getType().is(PortalCubedEntityTags.FIZZLES_WITHOUT_FLASH) || ticks <= EntityExt.TRANSLUCENCY_START_TICKS)
			return;

		matrices.pushPose();
		matrices.translate(0, entity.getBoundingBox().getYsize() / 2, 0);
		matrices.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
		matrices.scale(FLASH_SIZE, FLASH_SIZE, FLASH_SIZE);
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderType.beaconBeam(FLASH_TEXTURE, true));
		Matrix4f matrix = matrices.last().pose();
		int color = getFlashColor(ticks);
		flashVertex(vertices, matrix, 1f, 1f, color, 1, 1);
		flashVertex(vertices, matrix, 1f, -1f, color, 1, 0);
		flashVertex(vertices, matrix, -1f, -1f, color, 0, 0);
		flashVertex(vertices, matrix, -1f, 1f, color, 0, 1);
		matrices.popPose();
	}

	private static int getFlashColor(float ticks) {
		float value = ticks * FLASH_SPEED;
		float alpha = Mth.clamp(Mth.sin(value * 3) + Mth.cos(value * 2), MIN_FLASH_ALPHA, 1f);
		return ColorABGR.withAlpha(0xFFFFFF, ColorU8.normalizedFloatToByte(alpha));
	}

	private static void flashVertex(VertexConsumer vertexConsumer, Matrix4f matrix, float x, float y, int color, int textureU, int textureV) {
		vertexConsumer.vertex(matrix, x, y, 0)
				.color(color)
				.uv(textureU, textureV)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(LightTexture.FULL_BRIGHT)
				.normal(0, 1, 0)
				.endVertex();
	}

	// this allocates a lot, but it's probably not a problem unless there's like 1000+ disintegrating entities
	public static MultiBufferSource wrapVertexConsumers(Entity entity, float tickDelta, MultiBufferSource vertexConsumers) {
		return new MultiBufferSource() {
			private final float ticks = entity.pc$disintegrateTicks() + tickDelta;

			@NotNull
			@Override
			public VertexConsumer getBuffer(RenderType renderType) {
				// this won't work with non-translucent render types unless we use some sort of mapping, but there doesn't seem to be a good way to make a conversion map for entity render types
				return new DisintegrationVertexConsumer(vertexConsumers.getBuffer(renderType), ticks);
			}
		};
	}
}
