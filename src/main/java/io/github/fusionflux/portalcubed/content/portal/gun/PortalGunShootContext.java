package io.github.fusionflux.portalcubed.content.portal.gun;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.PortalCubedParticles;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public record PortalGunShootContext(
		String key,
		ServerLevel level,
		Vec3 from,
		Vec3 lookAngle,
		float yRot
) {
	// Magic number to avoid ray-cast clipping into blocks due to the end point being extremely far
	private static final double MAGIC_OFFSET = 0.35;

	public PortalGunShootContext(ServerPlayer player) {
		// TODO: this probably should use something other than the player's name
		this(player.getGameProfile().getName(), player.serverLevel(), player.getEyePosition(), player.getLookAngle(), player.getYRot());
	}

	public void shoot(Optional<String> pair, Polarity polarity, PortalSettings settings) {
		int range = this.level.getGameRules().getInt(PortalCubedGameRules.PORTAL_SHOT_RANGE_LIMIT);
		BlockHitResult hit = this.level.clip(new ClipContext(
				this.from.subtract(this.lookAngle.scale(MAGIC_OFFSET)),
				this.from.add(this.lookAngle.scale(range + MAGIC_OFFSET)),
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				CollisionContext.empty()
		));
		Vec3 hitPos = hit.getLocation();

		this.level.sendParticles(
				new CustomTrailParticleOption(PortalCubedParticles.PORTAL_PROJECTILE, hitPos, settings.color(), 3),
				this.from.x, this.from.y, this.from.z, 1, 0, 0, 0, 0
		);

		if (hit.getType() == HitResult.Type.BLOCK) {
			this.level.portalManager().createPortal(
					pair.orElse(this.key),
					polarity,
					PortalData.createWithSettings(this.level, hitPos, PortalData.normalToRotation(hit.getDirection(), this.yRot), settings)
			);
		}
	}
}
