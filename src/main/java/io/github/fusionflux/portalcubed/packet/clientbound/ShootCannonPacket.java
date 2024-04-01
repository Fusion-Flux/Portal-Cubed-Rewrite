package io.github.fusionflux.portalcubed.packet.clientbound;

import net.minecraft.client.CameraType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.content.cannon.CannonUseResult;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import io.github.fusionflux.portalcubed.framework.extension.ItemInHandRendererExt;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public record ShootCannonPacket(InteractionHand hand, CannonUseResult useResult) implements ClientboundPacket {
	public static final int PARTICLES = 10;

	public ShootCannonPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(InteractionHand.class), buf.readEnum(CannonUseResult.class));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(hand);
		buf.writeEnum(useResult);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.SHOOT_CANNON;
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		if (player.getItemInHand(hand).getItem() instanceof ConstructionCannonItem) {
			var itemInHandRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
			((ItemInHandRendererExt) itemInHandRenderer).pc$constructionCannonShoot(useResult);
			if (this.useResult == CannonUseResult.PLACED) {
				spawnParticlesForPlayer(player);
			}
		}
	}

	@ClientOnly
	public static void spawnParticlesForPlayer(Player player) {
		boolean thirdPerson = isThirdPerson(player);
		Vec3 source = thirdPerson ? getFirstPersonParticleSource(player) : getThirdPersonParticleSource(player);

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

	private static Vec3 getFirstPersonParticleSource(Player player) {
		var offset = new Vec3(-.5f, -.1f, 1.2f)
				.xRot(-player.getXRot() * Mth.DEG_TO_RAD)
				.yRot(-player.getYRot() * Mth.DEG_TO_RAD);
		return player.getEyePosition().add(offset);
	}

	private static Vec3 getThirdPersonParticleSource(Player player) {
		// FIXME figure these out in multiplayer
		var offset = new Vec3(0.5f, 0f, 1.9f)
				.xRot(-player.getXRot() * Mth.DEG_TO_RAD)
				.yRot(-player.getYRot() * Mth.DEG_TO_RAD);
		return player.getEyePosition().add(offset);
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
