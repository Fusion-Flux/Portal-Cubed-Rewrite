package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;

// Based on https://github.com/iPortalTeam/ImmersivePortalsMod/blob/55c9c1e7e298e09d8d43b0114e64e30271aa43b6/imm_ptl_core/src/main/java/qouteall/imm_ptl/core/mixin/client/render/framebuffer/MixinRenderTarget.java#L3
@Mixin(RenderTarget.class)
public class RenderTargetMixin {
	@Unique
	private final boolean isMain = (Object) this instanceof MainTarget;

	@ModifyArgs(
			method = "createBuffers",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
					remap = false
			)
	)
	private void texImage2D(Args args) {
		if (this.isMain && args.get(2).equals(GL11.GL_DEPTH_COMPONENT)) {
			args.set(2, GL30.GL_DEPTH24_STENCIL8);
			args.set(6, ARBFramebufferObject.GL_DEPTH_STENCIL);
			args.set(7, GL30.GL_UNSIGNED_INT_24_8);
		}
	}

	@ModifyArg(
			method = "createBuffers",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
					remap = false,
					ordinal = 1
			),
			index = 1
	)
	private int framebufferTexture2D(int original) {
		return this.isMain ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : original;
	}
}
