package io.github.fusionflux.portalcubed.framework.construct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.joml.Matrix4f;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;

import io.github.fusionflux.portalcubed.framework.model.TransformingBakedModel;
import io.github.fusionflux.portalcubed.framework.util.DelegatingVertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public final class ConstructModelPool implements AutoCloseable {
	private static final ModelEmitter EMITTER = new ModelEmitter();
	private static final Supplier<DynamicTexture> LIGHT_TEXTURE = Suppliers.memoize(() -> {
		DynamicTexture texture = new DynamicTexture(16, 16, false);
		Objects.requireNonNull(texture.getPixels()).fillRect(0, 0, 16, 16, 0xFFFFFFFF);
		texture.upload();
		return texture;
	});

	private final Object2ReferenceOpenHashMap<ConfiguredConstruct, ModelInfo> models = new Object2ReferenceOpenHashMap<>();

	public static ModelInfo buildModel(ConfiguredConstruct construct) {
		VirtualConstructEnvironment environment = new VirtualConstructEnvironment(construct);

		BlockRenderDispatcher renderDispatcher = Minecraft.getInstance().getBlockRenderer();
		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		RandomSource random = RandomSource.create();

		ModelBlockRenderer.enableCaching();
		PoseStack matrices = new PoseStack();
		construct.blocks.forEach((pos, info) -> {
			BlockState state = info.state();
			if (state.getRenderShape() == RenderShape.MODEL) {
				EMITTER.prepare(ItemBlockRenderTypes.getChunkRenderType(state), renderDispatcher.getBlockModel(state));
				matrices.pushPose();
				matrices.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderer.tesselateBlock(environment, EMITTER.model, state, pos, matrices, EMITTER, true, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY);
				matrices.popPose();
			}
		});
		ModelBlockRenderer.clearCache();

		List<ModelInfo.Buffer> buffers = new ArrayList<>();
		EMITTER.end(buffers::add);
		return new ModelInfo(buffers);
	}

	public ModelInfo getOrBuildModel(ConfiguredConstruct construct) {
		return this.models.computeIfAbsent(construct, $ -> buildModel(construct));
	}

	@Override
	public void close() {
		this.models.values().forEach(ModelInfo::close);
		this.models.clear();
	}

	public record ModelInfo(List<Buffer> buffers) implements AutoCloseable {
		public void draw(PoseStack matrices, Runnable extraRenderState) {
			Matrix4f matrix = matrices.last().pose();
			this.buffers.forEach(buffer -> buffer.draw(matrix, () -> {
				extraRenderState.run();
				RenderSystem.setShaderTexture(2, LIGHT_TEXTURE.get().getId());
			}));
		}

		@Override
		public void close() {
			this.buffers.forEach(Buffer::close);
			this.buffers.clear();
		}

		public record Buffer(RenderType renderType, VertexBuffer vertexBuffer) implements AutoCloseable {
			public void draw(Matrix4f matrix, Runnable extraRenderState) {
				this.renderType.setupRenderState();
				extraRenderState.run();
				this.vertexBuffer.bind();
				this.vertexBuffer.drawWithShader(matrix, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
				VertexBuffer.unbind();
				this.renderType.clearRenderState();
			}

			@Override
			public void close() {
				this.vertexBuffer.close();
			}
		}
	}

	private static final class ModelEmitter extends DelegatingVertexConsumer {
		private final Reference2ReferenceMap<RenderType, ByteBufferBuilder> buffers = Util.make(new Reference2ReferenceOpenHashMap<>(), map -> {
			for (RenderType renderType : RenderType.chunkBufferLayers()) {
				map.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
			}
		});
		private final Reference2ReferenceMap<RenderType, BufferBuilder> builders = new Reference2ReferenceOpenHashMap<>();
		private final DelegateModel model = new DelegateModel();

		private RenderType defaultRenderType;

		private void prepare(RenderType defaultRenderType, BakedModel model) {
			this.model.setDelegate(model);
			this.defaultRenderType = defaultRenderType;
		}

		private void end(Consumer<ModelInfo.Buffer> resultConsumer) {
			for (Map.Entry<RenderType, BufferBuilder> entry : this.builders.reference2ReferenceEntrySet()) {
				BufferBuilder builder = entry.getValue();
				MeshData meshData = builder.build();
				if (meshData != null) {
					VertexBuffer vertexBuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
					vertexBuffer.bind();
					vertexBuffer.upload(meshData);
					VertexBuffer.unbind();
					resultConsumer.accept(new ModelInfo.Buffer(entry.getKey(), vertexBuffer));
				}
			}

			this.builders.clear();
			this.model.setDelegate(null);
			this.defaultRenderType = null;
			this.delegate = null;
		}

		private void prepareForMaterial(RenderMaterial material) {
			BlendMode blendMode = material.blendMode();
			RenderType renderType = blendMode == BlendMode.DEFAULT ? ModelEmitter.this.defaultRenderType : blendMode.blockRenderLayer;
			this.delegate = ModelEmitter.this.builders.computeIfAbsent(renderType, $ -> new BufferBuilder(this.buffers.get(renderType), renderType.mode(), renderType.format()));
		}

		private final class DelegateModel extends TransformingBakedModel {
			private DelegateModel() {
				super((quad -> {
					ModelEmitter.this.prepareForMaterial(quad.material());
					return true;
				}));
			}

			private void setDelegate(BakedModel delegate) {
				this.delegate = delegate;
			}
		}
	}
}
