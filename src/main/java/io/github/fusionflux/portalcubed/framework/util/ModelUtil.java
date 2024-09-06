package io.github.fusionflux.portalcubed.framework.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.model.RenderMaterials;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class ModelUtil {
	private static final Direction[] FACES = Arrays.copyOf(Direction.values(), 7);

	public static TextureAtlasSprite getSprite(ResourceLocation texture) {
		return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
	}

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

	public static void emitVanillaQuads(
			BakedModel model,
			boolean item,
			RenderContext context,
			BiConsumer<BakedQuad, MutableQuadView> transformer,
			@Nullable BlockState state,
			Supplier<RandomSource> randomSupplier
	) {
		QuadEmitter emitter = context.getEmitter();
		RenderMaterial material = (model.useAmbientOcclusion() || item) ? RenderMaterials.STANDARD : RenderMaterials.NO_AO;
		for (Direction face : FACES) {
			if (!item && context.isFaceCulled(face))
				continue;
			List<BakedQuad> quads = model.getQuads(state, face, randomSupplier.get());
			for (BakedQuad quad : quads) {
				emitter.fromVanilla(quad, material, face);
				transformer.accept(quad, emitter);
				emitter.emit();
			}
		}
	}
}
