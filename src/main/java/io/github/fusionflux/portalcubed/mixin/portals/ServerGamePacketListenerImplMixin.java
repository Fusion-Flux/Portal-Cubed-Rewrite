package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.sync.tracker.TeleportTracker;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(
			method = "handleMovePlayer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerPlayer;isPassenger()Z"
			)
	)
	private void reinterpretMotion(CallbackInfo ci,
	                               @Local(ordinal = 0) LocalDoubleRef x, @Local(ordinal = 1) LocalDoubleRef y, @Local(ordinal = 2) LocalDoubleRef z,
	                               @Local(ordinal = 0) LocalFloatRef yRot, @Local(ordinal = 1) LocalFloatRef xRot) {
		PortalTransform transform = TeleportTracker.getOrThrow(this.player).reverseTransform();
		if (transform == null)
			return;

		Vec3 center = PortalTeleportHandler.centerOf(this.player);
		Vec3 posToCenter = this.player.position().vectorTo(center);

		Vec3 newPos = new Vec3(x.get(), y.get(), z.get());
		Vec3 newCenter = newPos.add(posToCenter);

		Vec3 transformedCenter = transform.applyAbsolute(newCenter);
		Vec3 newTeleportedPos = transformedCenter.subtract(posToCenter);
		x.set(newTeleportedPos.x);
		y.set(newTeleportedPos.y);
		z.set(newTeleportedPos.z);

		Rotations rotations = new Rotations(xRot.get(), yRot.get(), 0);
		Rotations transformedRotations = transform.apply(rotations);
		yRot.set(transformedRotations.getWrappedY());
		xRot.set(transformedRotations.getWrappedX());
	}
}
