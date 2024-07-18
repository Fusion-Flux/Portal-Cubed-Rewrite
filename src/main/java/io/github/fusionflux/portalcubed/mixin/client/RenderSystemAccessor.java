package io.github.fusionflux.portalcubed.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {
	@Accessor
	static void setModelViewStack(PoseStack matrices) {
		throw new AbstractMethodError();
	}

	@Accessor
	static void setModelViewMatrix(Matrix4f matrix) {
		throw new AbstractMethodError();
	}

	@Accessor
	static void setProjectionMatrix(Matrix4f matrix) {
		throw new AbstractMethodError();
	}

	@Accessor
	static Vector3f[] getShaderLightDirections() {
		throw new AbstractMethodError();
	}
}
