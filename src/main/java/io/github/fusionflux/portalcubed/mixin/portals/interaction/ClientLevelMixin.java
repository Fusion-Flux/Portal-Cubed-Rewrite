package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import java.util.OptionalDouble;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@WrapOperation(
			method = "playSound",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(DDD)D"
			)
	)
	private double findDistanceThroughPortals(Vec3 cameraPos, double x, double y, double z, Operation<Double> original,
											  @Local(argsOnly = true) SoundEvent sound, @Local(argsOnly = true, ordinal = 0) float volume) {
		double originalDistanceSqr = original.call(cameraPos, x, y, z);
		Vec3 soundPos = new Vec3(x, y, z);
		float range = sound.getRange(volume);

		OptionalDouble distanceSqr = PortalInteractionUtils.findPathLengthSqr((Level) (Object) this, soundPos, cameraPos, range);
		if (distanceSqr.isEmpty()) {
			return originalDistanceSqr;
		}

		return Math.min(originalDistanceSqr, distanceSqr.getAsDouble());
	}
}
