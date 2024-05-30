package io.github.fusionflux.portalcubed.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.framework.extension.ParticleEngineExt;
import io.github.fusionflux.portalcubed.framework.particle.DecalParticle;

import io.github.fusionflux.portalcubed.framework.particle.DecalParticleLightCache;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;

import net.minecraft.client.particle.ParticleRenderType;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;

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
public class ParticleEngineMixin implements ParticleEngineExt {
	@Shadow
	@Final
	@Mutable
	private static List<ParticleRenderType> RENDER_ORDER;

	private DecalParticleLightCache decalParticleLightCache;

	@Override
	public DecalParticleLightCache getDecalParticleLightCache() {
		return decalParticleLightCache;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void createDecalParticleLightCache(ClientLevel world, TextureManager textureManager, CallbackInfo ci) {
		this.decalParticleLightCache = new DecalParticleLightCache(world);
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void addMultiplyRenderType(CallbackInfo ci) {
		List<ParticleRenderType> newRenderOrder = new ArrayList<>(RENDER_ORDER);
		newRenderOrder.add(DecalParticle.PARTICLE_SHEET_MULTIPLY);
		RENDER_ORDER = Collections.unmodifiableList(newRenderOrder);
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void prepareDecalParticleLightCache(PoseStack matrices, MultiBufferSource.BufferSource bufferSource, LightTexture lightTexture, Camera camera, float tickDelta, CallbackInfo ci) {
		this.decalParticleLightCache.prepare();
	}

	@Inject(method = "setLevel", at = @At("TAIL"))
	private void recreateDecalParticleLightCache(ClientLevel world, CallbackInfo ci) {
		this.decalParticleLightCache = new DecalParticleLightCache(world);
	}
}
