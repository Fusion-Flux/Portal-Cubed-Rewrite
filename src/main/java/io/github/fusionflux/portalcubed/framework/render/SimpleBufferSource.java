package io.github.fusionflux.portalcubed.framework.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class SimpleBufferSource implements MultiBufferSource {
	private final Object2ReferenceMap<RenderType, ByteBufferBuilder> buffers = new Object2ReferenceArrayMap<>();
	private final Object2ReferenceMap<RenderType, BufferBuilder> builders = new Object2ReferenceArrayMap<>();

	private static ByteBufferBuilder createBuffer(RenderType type) {
		return new ByteBufferBuilder(type.bufferSize());
	}

	protected void setupRenderState(RenderType type) {
		type.setupRenderState();
	}

	@Override
	public final VertexConsumer getBuffer(RenderType type) {
		BufferBuilder buffer = this.builders.get(type);
		if (buffer != null) {
			if (type.canConsolidateConsecutiveGeometry()) {
				return buffer;
			} else {
				this.flush(type);
			}
		}

		buffer = new BufferBuilder(this.buffers.computeIfAbsent(type, SimpleBufferSource::createBuffer), type.mode(), type.format());
		this.builders.put(type, buffer);
		return buffer;
	}

	public final void flush(RenderType type) {
		BufferBuilder builder = this.builders.remove(type);
		if (builder == null)
			return;

		MeshData meshData = builder.build();
		if (meshData != null) {
			if (type.sortOnUpload())
				meshData.sortQuads(this.buffers.get(type), RenderSystem.getProjectionType().vertexSorting());

			this.setupRenderState(type);
			BufferUploader.drawWithShader(meshData);
			type.clearRenderState();
		}
	}

	public final void flush() {
		this.buffers.keySet().forEach(this::flush);
	}
}
