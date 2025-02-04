package io.github.fusionflux.portalcubed.framework.model.blendmode;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.model.RenderMaterials;
import io.github.fusionflux.portalcubed.framework.util.DelegatingQuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class MultiBlendModeBakedModel extends DelegateBakedModel {
	private static final Blender BLENDER = new Blender();

	public MultiBlendModeBakedModel(BakedModel parent) {
		super(parent);
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
		BLENDER.prepare(emitter);
		super.emitBlockQuads(BLENDER, blockView, state, pos, randomSupplier, cullTest);
		BLENDER.cleanup();
	}

	@Override
	public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
		BLENDER.prepare(emitter);
		super.emitItemQuads(BLENDER, randomSupplier);
		BLENDER.cleanup();
	}

	private static final class Blender extends DelegatingQuadEmitter {
		private void prepare(QuadEmitter delegate) {
			this.delegate = delegate;
		}

		private void cleanup() {
			this.delegate = null;
		}

		@Override
		public QuadEmitter fromVanilla(BakedQuad quad, RenderMaterial material, @Nullable Direction cullFace) {
			super.fromVanilla(
					quad,
					Optional.ofNullable(quad.pc$blendMode())
							.map(blendMode -> RenderMaterials.finder()
									.copyFrom(material)
									.blendMode(blendMode)
									.find()
							)
							.orElse(material),
					cullFace
			);
			return this;
		}
	}
}
