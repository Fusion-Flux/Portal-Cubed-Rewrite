package io.github.fusionflux.portalcubed.framework.model.dynamictexture;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;

public record DynamicTextureRenderData(Map<String, Identifier> map) {
	public static DynamicTextureRenderData.Builder builder() {
		return new DynamicTextureRenderData.Builder();
	}

	public static class Builder {
		private final Object2ObjectOpenHashMap<String, Identifier> map = new Object2ObjectOpenHashMap<>();

		Builder() {
		}

		public DynamicTextureRenderData.Builder set(String reference, Identifier texture) {
			this.map.put(reference, texture);
			return this;
		}

		public DynamicTextureRenderData build() {
			return new DynamicTextureRenderData(Object2ObjectMaps.unmodifiable(this.map));
		}
	}
}
