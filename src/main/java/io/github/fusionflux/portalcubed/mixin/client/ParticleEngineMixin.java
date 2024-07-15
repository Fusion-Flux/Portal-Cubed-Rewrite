package io.github.fusionflux.portalcubed.mixin.client;

import io.github.fusionflux.portalcubed.framework.particle.MultiplyParticleRenderType;
import net.minecraft.client.particle.ParticleEngine;

import net.minecraft.client.particle.ParticleRenderType;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	@Shadow
	@Final
	@Mutable
	private static List<ParticleRenderType> RENDER_ORDER;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void addMultiplyRenderType(CallbackInfo ci) {
		List<ParticleRenderType> newRenderOrder = new ArrayList<>(RENDER_ORDER);
		newRenderOrder.add(MultiplyParticleRenderType.INSTANCE);
		RENDER_ORDER = Collections.unmodifiableList(newRenderOrder);
	}
}
