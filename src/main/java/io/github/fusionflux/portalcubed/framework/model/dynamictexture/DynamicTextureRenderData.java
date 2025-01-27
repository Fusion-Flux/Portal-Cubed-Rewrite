package io.github.fusionflux.portalcubed.framework.model.dynamictexture;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

public record DynamicTextureRenderData(Map<String, ResourceLocation> map) {
	public static class Builder {
		private final Object2ObjectOpenHashMap<String, ResourceLocation> map = new Object2ObjectOpenHashMap<>();

		public DynamicTextureRenderData.Builder put(String reference, ResourceLocation texture) {
			this.map.put(reference, texture);
			return this;
		}

		public DynamicTextureRenderData build() {
			return new DynamicTextureRenderData(Object2ObjectMaps.unmodifiable(this.map));
		}
	}
}
