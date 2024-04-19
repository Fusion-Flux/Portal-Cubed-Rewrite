package io.github.fusionflux.portalcubed.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.extension.ClientboundTeleportEntityPacketExt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.framework.extension.AmbientSoundEmitter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "postAddEntitySoundInstance", at = @At("RETURN"))
	private void playCustomAmbientSounds(Entity entity, CallbackInfo ci) {
		if (!entity.isSilent() && entity instanceof AmbientSoundEmitter ambientSoundEmitter)
			ambientSoundEmitter.playAmbientSound();
	}

	@WrapOperation(
			method = "handleTeleportEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFIZ)V"
			)
	)
	private void maybeDontLerp(Entity entity,
							   double x, double y, double z,
							   float yaw, float pitch,
							   int interpolationSteps, boolean interpolate,
							   Operation<Void> original,
							   ClientboundTeleportEntityPacket packet) {
		boolean lerp = ((ClientboundTeleportEntityPacketExt) packet).pc$shouldLerp();
		Vec3 pos = new Vec3(x, y, z);
		System.out.println("Handling teleport to " + pos + ". lerp: " + lerp);
		if (lerp) {
			original.call(entity, x, y, z, yaw, pitch, interpolationSteps, interpolate);
		} else {
			entity.moveTo(x, y, z, yaw, pitch);
		}
	}
}
