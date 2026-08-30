package io.github.fusionflux.portalcubed.mixin.client;

import java.util.Optional;

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
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.sync.tracker.TeleportTracker;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.framework.extension.AmbientSoundEmitter;
import io.github.fusionflux.portalcubed.framework.util.Color;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "postAddEntitySoundInstance", at = @At("RETURN"))
	private void playCustomAmbientSounds(Entity entity, CallbackInfo ci) {
		if (!entity.isSilent() && entity instanceof AmbientSoundEmitter ambientSoundEmitter)
			ambientSoundEmitter.playAmbientSound();
	}

	// various packets need to be reinterpreted when a teleport is currently being tracked

	@Inject(
			method = "handleEntityPositionSync",
			at = @At(
					value = "INVOKE",
					// after positionCodec.setBase, but before the new pos is used
					target = "Lnet/minecraft/world/entity/Entity;position()Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private void reinterpretSync(ClientboundEntityPositionSyncPacket packet, CallbackInfo ci,
								 @Local Entity entity, @Local LocalRef<Vec3> pos,
								 @Local(ordinal = 0) LocalFloatRef xRot, @Local(ordinal = 1) LocalFloatRef yRot) {
		PortalTransform transform = getTransform(entity);
		if (transform == null)
			return;

		Vec3 center = PortalTeleportHandler.centerOf(entity);
		Vec3 posToCenter = entity.position().vectorTo(center);

		Vec3 newCenter = pos.get().add(posToCenter);
		Vec3 teleportedCenter = transform.applyAbsolute(newCenter);

		Vec3 newPos = teleportedCenter.subtract(posToCenter);

		Gizmos.point(pos.get(), Color.RED, 0.2f).persistForMillis(1000);
		Gizmos.point(newPos, Color.PURPLE, 0.2f).persistForMillis(1000);

		pos.set(newPos);

		Rotations rotations = transform.apply(xRot.get(), yRot.get());
		xRot.set(rotations.x());
		yRot.set(rotations.y());
	}

	@ModifyArgs(
			method = "handleMoveEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFI)V"
			)
	)
	private void reinterpretMove(Args args, @Local Entity entity, @Local(argsOnly = true) ClientboundMoveEntityPacket packet) {
		PortalTransform transform = getTransform(entity);
		if (transform == null)
			return;

		if (packet.hasPosition()) {
			Vec3 center = PortalTeleportHandler.centerOf(entity);
			Vec3 posToCenter = entity.position().vectorTo(center);

			Vec3 newPos = new Vec3(args.get(0), args.get(1), args.get(2));
			Vec3 newCenter = newPos.add(posToCenter);

			Vec3 transformedCenter = transform.applyAbsolute(newCenter);
			Vec3 newTeleportedPos = transformedCenter.subtract(posToCenter);
			args.set(0, newTeleportedPos.x);
			args.set(1, newTeleportedPos.y);
			args.set(2, newTeleportedPos.z);

			Gizmos.point(newCenter, Color.GREEN, 0.2f).persistForMillis(500);
			Gizmos.point(transformedCenter, Color.BLUE, 0.2f).persistForMillis(500);
		}

		if (packet.hasRotation()) {
			// why is it yx here instead of xy
			Rotations rotations = new Rotations(args.get(4), args.get(3), 0);
			Rotations transformedRotations = transform.apply(rotations);
			args.set(4, transformedRotations.x());
			args.set(3, transformedRotations.y());
		}
	}

	@ModifyArgs(
			method = "handleSetEntityMotion",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;lerpMotion(DDD)V"
			)
	)
	private void reinterpretVelocity(Args args, @Local Entity entity) {
		PortalTransform transform = getTransform(entity);
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
	private float reinterpretHeadRot(float original, @Local Entity entity) {
		PortalTransform transform = getTransform(entity);
		if (transform == null)
			return original;

		return transform.apply(original, Direction.Axis.Y);
	}

	@Unique
	@Nullable
	private static PortalTransform getTransform(Entity entity) {
		Optional<TeleportTracker> tracker = TeleportTracker.of(entity);
		return tracker.isEmpty() ? null : tracker.get().reverseTransform();
	}
}
