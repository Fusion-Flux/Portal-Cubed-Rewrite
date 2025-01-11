package io.github.fusionflux.portalcubed.mixin.client;

import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {
	@Accessor
	static void setModelViewStack(Matrix4fStack matrices) {
		throw new AbstractMethodError();
	}

	@Accessor
	static Vector3f[] getShaderLightDirections() {
		throw new AbstractMethodError();
	}
}
