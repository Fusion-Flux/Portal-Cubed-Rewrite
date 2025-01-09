package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin {
	@Shadow
	@Final
	private List<Uniform> uniforms;

	@Shadow
	@Final
	private Program vertexProgram;

	@Shadow
	@Final
	private Program fragmentProgram;


	@Unique
	private Uniform clippingPlane;

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderInstance;updateLocations()V"))
	private void addClippingPlaneUniform(ResourceProvider factory, String name, VertexFormat format, CallbackInfo ci) {
		if (ShaderPatcher.shouldPatch(this.vertexProgram.getName() + ".vsh") || ShaderPatcher.shouldPatch(this.fragmentProgram.getName() + ".fsh")) {
			this.clippingPlane = new Uniform(ShaderPatcher.CLIPPING_PLANE_UNIFORM_NAME, Uniform.UT_FLOAT4, 4, (ShaderInstance) (Object) this);
			this.uniforms.add(this.clippingPlane);
		}
	}

	@Inject(method = "apply", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_activeTexture(I)V", shift = At.Shift.AFTER))
	private void updateClippingPlaneUniform(CallbackInfo ci) {
		if (this.clippingPlane != null && PortalRenderer.isRenderingView())
			this.clippingPlane.set(PortalRenderer.CLIPPING_PLANES.get());
	}
}
