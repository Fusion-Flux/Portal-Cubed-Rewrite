package io.github.fusionflux.portalcubed.content.portal.graphics.render.stencil;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.ARBDepthClamp;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.graphics.render.PortalRenderer;
import io.github.fusionflux.portalcubed.content.portal.graphics.render.PortalTextureManager;
import io.github.fusionflux.portalcubed.framework.render.PortalCubedRenderTypes;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import io.github.fusionflux.portalcubed.framework.util.SimpleSynchronousReloadListener;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;

public final class PortalStencilRenderer implements SimpleSynchronousReloadListener {
	public static final PortalStencilRenderer INSTANCE = new PortalStencilRenderer();
	public static final float DEPTH = (float) -(0.5 + PortalRenderer.OFFSET_FROM_WALL);

	public static final ResourceLocation ID = PortalCubed.id("portal_stencils");
	public static final Collection<ResourceLocation> DEPENDENCIES = List.of(PortalTextureManager.ID);

	private static final Logger logger = LogUtils.getLogger();

	private Context context ;

	private PortalStencilRenderer() {}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public Collection<ResourceLocation> getFabricDependencies() {
		return DEPENDENCIES;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		if (this.context != null)
			this.context.close();
		this.context = new Context(manager);
	}

	public void render(RenderStateShard.DepthTestStateShard depthTest, PortalType.Stencil asset, boolean inside, Matrix4f matrix) {
		if (this.context == null)
			return;

		//noinspection resource
		FrontBackBuffer frontBack = this.context.getOrBake(asset).orElse(null);
		if (frontBack == null)
			return;

		GL11.glEnable(ARBDepthClamp.GL_DEPTH_CLAMP);

		Matrix4fStack matrices = RenderSystem.getModelViewStack();
		matrices.pushMatrix();
		matrices.mul(matrix);

		frontBack.front.drawWithRenderType(PortalCubedRenderTypes.portalStencil(depthTest, asset.frontTexturePath()));
		if (inside) {
			frontBack.back.drawWithRenderType(PortalCubedRenderTypes.portalStencil(depthTest, asset.backTexturePath()));
		}

		matrices.popMatrix();

		GL11.glDisable(ARBDepthClamp.GL_DEPTH_CLAMP);
		RenderSystem.enableDepthTest();
	}

	private record FrontBackBuffer(VertexBuffer front, VertexBuffer back) implements AutoCloseable {
		@Override
		public void close() {
			this.front.close();
			this.back.close();
		}
	}

	private record Context(ResourceManager resourceManager, ByteBufferBuilder scratchBuffer, Map<PortalType.Stencil, Optional<FrontBackBuffer>> buffers) implements AutoCloseable {
		private Context(ResourceManager resourceManager) {
			this(resourceManager, new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE), new HashMap<>());
		}

		private BufferBuilder begin(float depth) {
			BufferBuilder builder = new BufferBuilder(this.scratchBuffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			builder.addVertex(1, depth, 0).setUv(0, 1);
			builder.addVertex(0, depth, 0).setUv(1, 1);
			builder.addVertex(0, depth, 2).setUv(1, 0);
			builder.addVertex(1, depth, 2).setUv(0, 0);
			return builder;
		}

		private Optional<FrontBackBuffer> bake(PortalType.Stencil asset) {
			// Front
			if (this.resourceManager.getResource(asset.frontTexturePath()).isEmpty()) {
				logger.error("Failed to load {}", asset.front());
				return Optional.empty();
			}

			VertexBuffer front = RenderingUtils.uploadStaticMesh(this.begin(0));

			// Back
			PortalStencilShape shape;
			try (NativeImage image = NativeImage.read(this.resourceManager.open(asset.backTexturePath()))) {
				shape = PortalStencilShape.generate(image.getWidth(), image.getHeight(), (x, y) -> ARGB.alpha(image.getPixel(x, y)) > 0);
			} catch (IOException e) {
				logger.error("Failed to load {}", asset.back(), e);
				return Optional.empty();
			}

			BufferBuilder builder = this.begin(DEPTH);

			double width = shape.width();
			double height = shape.height();
			float localToWorld = (float) (1 / Math.min(width, height));
			for (PortalStencilShape.Segment segment : shape) {
				double x0 = segment.x();
				double y0 = segment.y();
				double x1 = segment instanceof PortalStencilShape.Segment.Horizontal horizontal ? horizontal.x2() + 1 : x0 + 1;
				double y1 = segment instanceof PortalStencilShape.Segment.Vertical vertical ? vertical.y2() + 1 : y0 + 1;

				if (x1 <= x0 || y1 <= y0) {
					logger.error("Zero volume from {},{} to {},{} in {}", x0, y0, x1, y1, asset.back());
					return Optional.empty();
				}

				float u0 = (float) (x0 / width);
				float v0 = (float) (y0 / height);
				float u1 = (float) (x1 / width);
				float v1 = (float) (y1 / height);

				float worldMinX = (float) (x0 * localToWorld);
				float worldMaxX = (float) (x1 * localToWorld);
				float worldMinZ = (float) (y0 * localToWorld);
				float worldMaxZ = (float) (y1 * localToWorld);

				for (PortalStencilSide side : segment.sides()) {
					int i = 0;
					for (PortalStencilSide.Vertex vertex : side.vertices()) {
						float u = (i % 3) == 0 ? u0 : u1;
						float v = i > 1 ? v0 : v1;

						builder.addVertex(
								vertex.x() ? worldMaxX : worldMinX,
								vertex.y() ? DEPTH : 0,
								vertex.z() ? worldMaxZ : worldMinZ
						).setUv(u, v);
						i++;
					}
				}
			}

			VertexBuffer back = RenderingUtils.uploadStaticMesh(builder);


			return Optional.of(new FrontBackBuffer(front, back));
		}

		public Optional<FrontBackBuffer> getOrBake(PortalType.Stencil asset) {
			return this.buffers.computeIfAbsent(asset, this::bake);
		}

		@Override
		public void close() {
			this.buffers.values().forEach(v -> v.ifPresent(FrontBackBuffer::close));
			this.scratchBuffer.close();
		}
	}
}
