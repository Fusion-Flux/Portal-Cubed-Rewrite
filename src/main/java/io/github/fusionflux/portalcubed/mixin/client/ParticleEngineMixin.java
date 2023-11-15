package io.github.fusionflux.portalcubed.mixin.client;

import com.google.common.collect.ImmutableList;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.particle.DecalParticle;

import net.minecraft.client.particle.ParticleEngine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	@WrapOperation(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;",
					remap = false
			)
	)
	private static ImmutableList<?> addRenderTypes(
			Object e1, Object e2, Object e3, Object e4, Object e5, Operation<ImmutableList<?>> original
	) {
		return Stream.concat(
				original.call(e1, e2, e3, e4, e5).stream(),
				Stream.of(DecalParticle.PARTICLE_SHEET_MULTIPLY)
		).collect(ImmutableList.toImmutableList());
	}
}
