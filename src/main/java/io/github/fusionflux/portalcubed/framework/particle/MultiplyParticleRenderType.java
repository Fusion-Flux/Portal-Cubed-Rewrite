package io.github.fusionflux.portalcubed.framework.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;

import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public class MultiplyParticleRenderType implements ParticleRenderType {
	public static final MultiplyParticleRenderType INSTANCE = new MultiplyParticleRenderType();

	public static final String NAME = PortalCubed.id("multiply").toString();

	@Override
	public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.SRC_COLOR);
		RenderSystem.depthMask(false);
		RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
	}

	@Override
	public void end(Tesselator tessellator) {
		tessellator.end();
		// Without this fluid overlays (lava fire and water screen overlay) break while holding a block item
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(true);
	}

	@Override
	public String toString() {
		return NAME;
	}
}
