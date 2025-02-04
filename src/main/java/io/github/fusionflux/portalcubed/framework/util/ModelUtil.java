package io.github.fusionflux.portalcubed.framework.util;

import java.util.Arrays;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class ModelUtil {
	private static final Direction[] FACES = Arrays.copyOf(Direction.values(), 7);

	@SuppressWarnings("deprecation")
	public static TextureAtlasSprite getSprite(ResourceLocation texture) {
		return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
	}

	@SuppressWarnings("deprecation")
	public static SpriteFinder getSpriteFinder() {
		ModelManager modelManager = Minecraft.getInstance().getModelManager();
		return SpriteFinder.get(modelManager.getAtlas(TextureAtlas.LOCATION_BLOCKS));
	}

	public static void normalizeUV(MutableQuadView quad, TextureAtlasSprite sprite) {
		float uMin = sprite.getU0();
		float uSpan = sprite.getU1() - uMin;
		float vMin = sprite.getV0();
		float vSpan = sprite.getV1() - vMin;
		for (int i = 0; i < 4; i++) {
			quad.uv(i, (quad.u(i) - uMin) / uSpan, (quad.v(i) - vMin) / vSpan);
		}
	}
}
