package io.github.fusionflux.portalcubed.mixin;

import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.content.portal.TeleportStep;
import io.github.fusionflux.portalcubed.framework.util.RangeSequence;

import io.github.fusionflux.portalcubed.packet.clientbound.PortalTeleportPacket;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
	@Shadow
	@Final
	private Entity entity;

	@Shadow
	@Final
	private Consumer<Packet<?>> broadcast;

	@Shadow
	private int teleportDelay;

	@Shadow
	private boolean wasOnGround;

	@Shadow
	private Vec3 ap;

	@Shadow
	protected abstract void sendDirtyEntityData();

	@Shadow
	@Final
	private VecDeltaCodec positionCodec;

	@Shadow
	private int yRotp;

	@Shadow
	private int xRotp;

	@Shadow
	private boolean wasRiding;

	@Shadow
	private int yHeadRotp;

	@Shadow
	private int tickCount;

	@Inject(
			method = "sendChanges",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/server/level/ServerEntity;updateInterval:I"
			),
			cancellable = true
	)
	private void sendPortalTeleport(CallbackInfo ci) {
		RangeSequence<TeleportStep> steps = this.entity.getPortalTeleport();
		if (steps == null)
			return;

		// main packet
		PortalTeleportPacket payload = new PortalTeleportPacket(this.entity.getId(), steps);
		Packet<ClientCommonPacketListener> packet = ServerPlayNetworking.createS2CPacket(payload);
		this.broadcast.accept(packet);

		// update all values and sync other stuff we have to cancel because sendChanges is nightmare spaghetti
		// entity should never be a passenger - that block is skipped
		this.wasOnGround = this.entity.onGround();
		this.teleportDelay = 0;
		this.ap = this.entity.getDeltaMovement();
		this.sendDirtyEntityData();
		this.positionCodec.setBase(this.entity.trackingPosition());
		this.yRotp = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
		this.xRotp = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
		this.wasRiding = false;
		this.yHeadRotp = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
		this.tickCount++;

		// sync has been handled
		ci.cancel();
		this.entity.hasImpulse = false;
		this.entity.hurtMarked = false;
		this.entity.setPortalTeleport(null);
	}
}
