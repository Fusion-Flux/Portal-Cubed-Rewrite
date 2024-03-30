package io.github.fusionflux.portalcubed.framework.construct;

import java.util.HashSet;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;

public final class ConstructModelPool implements AutoCloseable {
	private static final Map<RenderType, BufferBuilder> BUILDERS = new Object2ReferenceOpenHashMap<>();
	private final Reference2ReferenceOpenHashMap<ConfiguredConstruct, ModelInfo> models = new Reference2ReferenceOpenHashMap<>();

	public static ModelInfo buildModel(ConfiguredConstruct construct) {
		var environment = new VirtualConstructEnvironment(construct);
		var usedRenderTypes = new HashSet<RenderType>();
		var buffers = new Reference2ReferenceOpenHashMap<RenderType, VertexBuffer>();
		var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
		var randomSource = RandomSource.create();

		ModelBlockRenderer.enableCaching();

		var matrices = new PoseStack();
		construct.blocks.forEach((pos, info) -> {
			var state = info.state();
			if (state.getRenderShape() == RenderShape.MODEL) {
				var renderType = ItemBlockRenderTypes.getChunkRenderType(state);
				var builder = BUILDERS.computeIfAbsent(renderType, $ -> new BufferBuilder(renderType.bufferSize()));
				if (usedRenderTypes.add(renderType))
					builder.begin(renderType.mode(), renderType.format());

				matrices.pushPose();
				matrices.translate(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
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

		return new ModelInfo(buffers);
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

	public static record ModelInfo(Reference2ReferenceOpenHashMap<RenderType, VertexBuffer> buffers) implements AutoCloseable {
		public void render(PoseStack matrices) {
			RenderSystem.disableDepthTest();
			GL11.glEnable(GL32.GL_DEPTH_CLAMP);
			for (var entry : buffers.reference2ReferenceEntrySet()) {
				var renderType = entry.getKey();
				var buffer = entry.getValue();
				renderType.setupRenderState();
				buffer.bind();
				buffer.drawWithShader(matrices.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
				renderType.clearRenderState();
			}
			GL11.glDisable(GL32.GL_DEPTH_CLAMP);
			VertexBuffer.unbind();
			RenderSystem.enableDepthTest();
		}

		@Override
		public void close() {
			buffers.values().forEach(VertexBuffer::close);
			buffers.clear();
		}
	}
}
