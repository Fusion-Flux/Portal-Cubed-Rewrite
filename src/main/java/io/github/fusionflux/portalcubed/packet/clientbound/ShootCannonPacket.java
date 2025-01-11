package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.cannon.CannonUseResult;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonAnimator;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ShootCannonPacket(InteractionHand hand, CannonUseResult useResult) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, ShootCannonPacket> CODEC = StreamCodec.composite(
			PortalCubedStreamCodecs.HAND, ShootCannonPacket::hand,
			CannonUseResult.STREAM_CODEC, ShootCannonPacket::useResult,
			ShootCannonPacket::new
	);

	// offsets found through trial and error
	public static final Vec3 FIRST_PERSON_OFFSET = new Vec3(-.5f, -.1f, 1.2f);
	public static final Vec3 THIRD_PERSON_OFFSET = new Vec3(-0.1f, -0.4f, 1.9f);
	public static final int PARTICLES = 10;

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.SHOOT_CANNON;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		Player player = ctx.player();
		if (player.getItemInHand(this.hand).getItem() instanceof ConstructionCannonItem) {
			ConstructionCannonAnimator.onShoot(this.useResult);
			if (this.useResult == CannonUseResult.PLACED)
				spawnParticlesForPlayer(player);
		}
	}

	@Environment(EnvType.CLIENT)
	public static void spawnParticlesForPlayer(Player player) {
		boolean thirdPerson = isThirdPerson(player);
		Vec3 baseOffset = thirdPerson ? THIRD_PERSON_OFFSET : FIRST_PERSON_OFFSET;
		Vec3 rotatedOffset = baseOffset
				.xRot(-player.getXRot() * Mth.DEG_TO_RAD)
				.yRot(-player.getYRot() * Mth.DEG_TO_RAD);
		Vec3 source = player.getEyePosition().add(rotatedOffset);

		for (int i = 0; i < PARTICLES; i++) {
			Vec3 target = getParticleTarget(player);
			Vec3 vel = getParticleVelocity(source, target);

			player.level().addParticle(
					ParticleTypes.POOF,
					source.x, source.y, source.z,
					vel.x, vel.y, vel.z
			);
		}
	}

	private static boolean isThirdPerson(Player player) {
		if (!player.isLocalPlayer())
			return true;
		CameraType cameraType = Minecraft.getInstance().options.getCameraType();
		return !cameraType.isFirstPerson();
	}

	private static Vec3 getParticleTarget(Player player) {
		Vec3 lookingAt = player.getViewVector(1).scale(3);
		Vec3 target = player.getEyePosition().add(lookingAt);
		// random offset
		RandomSource random = player.getRandom();
		return target.offsetRandom(random, 0.2f);
	}

	public static Vec3 getParticleVelocity(Vec3 source, Vec3 target) {
		return source.vectorTo(target).scale(0.1);
	}
}
