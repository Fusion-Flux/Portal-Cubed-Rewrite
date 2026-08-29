package io.github.fusionflux.portalcubed.framework.util;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

public class ModelUtil {
	public static TextureAtlasSprite getSprite(Identifier texture) {
		//noinspection deprecation
		return Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, texture));
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
