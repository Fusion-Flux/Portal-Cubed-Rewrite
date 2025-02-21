package io.github.fusionflux.portalcubed.framework.render;

import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public record SimpleBufferSource(Object2ReferenceMap<RenderType, ByteBufferBuilder> buffers, Object2ReferenceMap<RenderType, BufferBuilder> builders) implements MultiBufferSource {
	public SimpleBufferSource(Iterable<RenderType> renderTypes) {
		this(new Object2ReferenceOpenHashMap<>(), new Object2ReferenceLinkedOpenHashMap<>());
		this.buffers.defaultReturnValue(new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE));
		renderTypes.forEach(renderType -> this.buffers.put(renderType, new ByteBufferBuilder(renderType.bufferSize())));
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		return this.builders.computeIfAbsent(
				renderType,
				$ -> new BufferBuilder(this.buffers.get(renderType), renderType.mode(), renderType.format())
		);
	}

	public void flush() {
		for (Map.Entry<RenderType, BufferBuilder> entry : this.builders.object2ReferenceEntrySet()) {
			BufferBuilder builder = entry.getValue();
			MeshData meshData = builder.build();
			if (meshData != null) {
				RenderType renderType = entry.getKey();
				if (renderType.sortOnUpload())
					meshData.sortQuads(this.buffers.get(renderType), RenderSystem.getProjectionType().vertexSorting());

				renderType.setupRenderState();
				BufferUploader.drawWithShader(meshData);
				renderType.clearRenderState();
			}
		}
		this.builders.clear();
	}
}
