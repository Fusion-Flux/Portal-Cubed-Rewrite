package io.github.fusionflux.portalcubed.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.sync.TeleportProgressTracker;
import io.github.fusionflux.portalcubed.framework.extension.AmbientSoundEmitter;
import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import io.github.fusionflux.portalcubed.framework.util.Color;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "postAddEntitySoundInstance", at = @At("RETURN"))
	private void playCustomAmbientSounds(Entity entity, CallbackInfo ci) {
		if (!entity.isSilent() && entity instanceof AmbientSoundEmitter ambientSoundEmitter)
			ambientSoundEmitter.playAmbientSound();
	}

	// various packets need to be reinterpreted when teleporting through a portal
	// ClientboundTeleportEntityPacket (when local)
	// ClientboundMoveEntityPacket.Pos
	// ClientboundMoveEntityPacket.Rot
	// ClientboundMoveEntityPacket.PosRot
	// ClientboundSetEntityMotionPacket
	// ClientboundRotateHeadPacket

	@Inject(
			method = "handleEntityPositionSync",
			at = @At(
					value = "INVOKE",
					// after positionCodec.setBase, but before the new pos is used
					target = "Lnet/minecraft/world/entity/PositionMoveRotation;yRot()F"
			)
	)
	private void reinterpretSync(ClientboundEntityPositionSyncPacket packet, CallbackInfo ci,
								 @Local Entity entity, @Local LocalRef<Vec3> pos) {
		PortalTransform transform = getPortalTransform(entity);
		if (transform == null)
			return;

		Vec3 center = PortalTeleportHandler.centerOf(entity);
		Vec3 posToCenter = entity.position().vectorTo(center);

		Vec3 newCenter = pos.get().add(posToCenter);
		Vec3 teleportedCenter = transform.applyAbsolute(newCenter);

		Vec3 newPos = teleportedCenter.subtract(posToCenter);

		DebugRendering.addPos(20, pos.get(), Color.RED);
		DebugRendering.addPos(20, newPos, Color.PURPLE);

		pos.set(newPos);

//		Rotations newRots = PortalTeleportHandler.teleportRotations(yaw, pitch, 0, portals.getSecond(), portals.getFirst());
//		original.call(entity, newPos.x, newPos.y, newPos.z, newRots.getWrappedX(), newRots.getWrappedY(), steps);
	}

	@ModifyArgs(
			method = "handleMoveEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFI)V",
					ordinal = 0
			)
	)
	private void reinterpretMovePos(Args args, @Local Entity entity) {
		PortalTransform transform = getPortalTransform(entity);
		if (transform == null)
			return;

		Vec3 center = PortalTeleportHandler.centerOf(entity);
		Vec3 posToCenter = entity.position().vectorTo(center);

		Vec3 newPos = new Vec3(args.get(0), args.get(1), args.get(2));
		Vec3 newCenter = newPos.add(posToCenter);

		Vec3 transformedCenter = transform.applyAbsolute(newCenter);
		Vec3 newTeleportedPos = transformedCenter.subtract(posToCenter);
		args.set(0, newTeleportedPos.x);
		args.set(1, newTeleportedPos.y);
		args.set(2, newTeleportedPos.z);

		DebugRendering.addPos(10, newCenter, Color.GREEN);
		DebugRendering.addPos(10, transformedCenter, Color.BLUE);
	}

	@ModifyArgs(
			method = "handleSetEntityMotion",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;lerpMotion(DDD)V"
			)
	)
	private void reinterpretVelocity(Args args, @Local Entity entity) {
		PortalTransform transform = getPortalTransform(entity);
		if (transform == null)
			return;

		Vec3 vel = new Vec3(args.get(0), args.get(1), args.get(2));
		Vec3 newVel = transform.applyRelative(vel);
		args.set(0, newVel.x);
		args.set(1, newVel.y);
		args.set(2, newVel.z);
	}

	@ModifyArg(
			method = "handleRotateMob",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;lerpHeadTo(FI)V"
			)
	)
	private float reinterpretHeadRot(float yaw) {
		return yaw;
	}

	@Unique
	@Nullable
	private static PortalTransform getPortalTransform(Entity entity) {
		TeleportProgressTracker tracker = entity.getTeleportProgressTracker();
		return tracker.isTracking() ? tracker.currentTeleport().transform : null;
	}
}
