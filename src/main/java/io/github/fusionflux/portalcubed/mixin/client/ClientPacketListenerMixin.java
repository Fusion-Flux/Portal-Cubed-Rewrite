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

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.sync.TeleportProgressTracker;
import io.github.fusionflux.portalcubed.framework.extension.AmbientSoundEmitter;
import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import io.github.fusionflux.portalcubed.framework.util.Color;
import net.minecraft.client.multiplayer.ClientPacketListener;
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

	// TODO: This whole thing needs a rewrite
//	@WrapOperation(
//			method = "handleTeleportEntity",
//			at = @At(
//					value = "INVOKE",
//					target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFI)V"
//			)
//	)
//	private void reinterpretTeleport(Entity entity, double x, double y, double z, float yaw, float pitch,
//									 int steps, Operation<Void> original,
//									 @Local(argsOnly = true) ClientboundTeleportEntityPacket packet) {
//		if (!packet.pc$isLocal()) {
//			// non-local tp packet, abort tracking
//			entity.setTeleportProgressTracker(null);
//			original.call(entity, x, y, z, yaw, pitch, steps);
//			return;
//		}
//
//		// need to teleport values backwards to make the entity try to go through the portal
//		// all portals between the first entered and last exited can be ignored
//		Pair<PortalInstance, PortalInstance> portals = getFirstAndLastPortals(entity);
//		if (portals == null) {
//			original.call(entity, x, y, z, yaw, pitch, steps);
//			return;
//		}
//
//		Vec3 center = PortalTeleportHandler.centerOf(entity);
//		Vec3 posToCenter = entity.position().vectorTo(center);
//
//		Vec3 pos = new Vec3(x, y, z);
//		Vec3 newCenter = pos.add(posToCenter);
//
//		Vec3 teleportedCenter = PortalTeleportHandler.teleportAbsoluteVecBetween(newCenter, portals.getSecond(), portals.getFirst());
//		Vec3 newPos = teleportedCenter.subtract(posToCenter);
//		DebugRendering.addPos(20, pos, Color.RED);
//		DebugRendering.addPos(20, newPos, Color.PURPLE);
//
//		Rotations newRots = PortalTeleportHandler.teleportRotations(yaw, pitch, 0, portals.getSecond(), portals.getFirst());
//		original.call(entity, newPos.x, newPos.y, newPos.z, newRots.getWrappedX(), newRots.getWrappedY(), steps);
//	}

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

		DebugRendering.addPos(10, newCenter, Color.RED);
		DebugRendering.addPos(10, transformedCenter, Color.GREEN);

		Vec3 outOrigin = transform.applyAbsolute(transform.inOrigin);
		DebugRendering.addPos(10, outOrigin, Color.PURPLE);
		DebugRendering.addPos(10, transform.inOrigin, Color.CYAN);
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
