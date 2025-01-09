package io.github.fusionflux.portalcubed.mixin.client;

import net.minecraft.client.Camera;

import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraAccessor {
	// Do not call this set position or else it will override vanilla's
	@Accessor("position")
	void pc$setPosition(Vec3 position);

	@Accessor
	void setRotation(Quaternionf rotation);
}
