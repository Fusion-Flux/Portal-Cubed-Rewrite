package io.github.fusionflux.portalcubed.framework.construct;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ConstructModelPool implements AutoCloseable {
	private static final Map<RenderType, BufferBuilder> BUILDERS = new Object2ReferenceOpenHashMap<>();
	private static final Supplier<DynamicTexture> FAKE_LIGHT_TEXTURE = Suppliers.memoize(() -> {
		var texture = new DynamicTexture(16, 16, false);
		var pixels = texture.getPixels();

		for(int x = 0; x < 16; x++) {
			for(int y = 0; y < 16; y++) {
				pixels.setPixelRGBA(x, y, 0xFFFFFF);
			}
		}

		texture.upload();
		return texture;
	});
	private final Reference2ReferenceOpenHashMap<ConfiguredConstruct, ModelInfo> models = new Reference2ReferenceOpenHashMap<>();

	public static ModelInfo buildModel(ConfiguredConstruct construct) {
		var environment = new VirtualConstructEnvironment(construct);
		var usedRenderTypes = new HashSet<RenderType>();
		var blockEntities = new HashSet<BlockEntity>();
		var buffers = new Reference2ReferenceOpenHashMap<RenderType, VertexBuffer>();
		var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
		var randomSource = RandomSource.create();

		ModelBlockRenderer.enableCaching();

		var matrices = new PoseStack();
		construct.blocks.forEach((pos, info) -> {
			var state = info.state();
			if (state.hasBlockEntity())
				blockEntities.add(environment.getBlockEntity(pos));
			if (state.getRenderShape() == RenderShape.MODEL) {
				var renderType = ItemBlockRenderTypes.getChunkRenderType(state);
				var builder = BUILDERS.computeIfAbsent(renderType, $ -> new BufferBuilder(renderType.bufferSize()));
				if (usedRenderTypes.add(renderType))
					builder.begin(renderType.mode(), renderType.format());

				matrices.pushPose();
				matrices.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderDispatcher.renderBatched(state, pos, environment, matrices, builder, true, randomSource);
				matrices.popPose();
			}
		});

		for (var renderType : usedRenderTypes) {
			var builtBuffer = BUILDERS.get(renderType).endOrDiscardIfEmpty();
			if (builtBuffer != null) {
				var vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
				vertexBuffer.bind();
				vertexBuffer.upload(builtBuffer);
				buffers.put(renderType, vertexBuffer);
			}
		}
		VertexBuffer.unbind();

		ModelBlockRenderer.clearCache();

		return new ModelInfo(blockEntities, buffers);
	}

	@SuppressWarnings("resource")
	public ModelInfo getOrBuildModel(ConfiguredConstruct construct) {
		return models.computeIfAbsent(construct, $ -> buildModel(construct));
	}

	@Override
	public void close() {
		models.values().forEach(ModelInfo::close);
		models.clear();
	}

	public record ModelInfo(Set<BlockEntity> blockEntities, Reference2ReferenceMap<RenderType, VertexBuffer> buffers) implements AutoCloseable {
		@SuppressWarnings("resource")
		public void draw(PoseStack matrices, Runnable extraRenderState) {
			for (var entry : buffers.reference2ReferenceEntrySet()) {
				var renderType = entry.getKey();
				var buffer = entry.getValue();
				renderType.setupRenderState();
				RenderSystem.setShaderTexture(2, FAKE_LIGHT_TEXTURE.get().getId());
				extraRenderState.run();
				buffer.bind();
				buffer.drawWithShader(matrices.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
				renderType.clearRenderState();
			}
			Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
			VertexBuffer.unbind();
		}

		public void bufferBlockEntities(PoseStack matrices, MultiBufferSource.BufferSource bufferSource) {
			var blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
			for (var blockEntity : blockEntities) {
				var pos = blockEntity.getBlockPos();
				matrices.pushPose();
				matrices.translate(pos.getX(), pos.getY(), pos.getZ());
				blockEntityRenderDispatcher.renderItem(blockEntity, matrices, bufferSource, LightTexture.FULL_SKY, OverlayTexture.NO_OVERLAY);
				matrices.popPose();
			}
		}

		@Override
		public void close() {
			buffers.values().forEach(VertexBuffer::close);
			buffers.clear();
		}
	}
}
