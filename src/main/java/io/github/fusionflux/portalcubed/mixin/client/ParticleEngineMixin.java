package io.github.fusionflux.portalcubed.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.framework.particle.PortalCubedParticleRenderTypes;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	@Shadow
	@Final
	@Mutable
	private static List<ParticleRenderType> RENDER_ORDER;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void addMultiplyRenderType(CallbackInfo ci) {
		RENDER_ORDER = new ArrayList<>(RENDER_ORDER);
		RENDER_ORDER.add(PortalCubedParticleRenderTypes.MULTIPLY);
	}
}
