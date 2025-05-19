package io.github.fusionflux.portalcubed.content.portal.gun;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.PortalCubedParticles;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalBumper;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalCollisionContext;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalPlacement;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalShotClipContextMode;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.StandardPortalValidator;
import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
		BlockHitResult hit = clip(this.level, this.from, this.lookAngle);
		Vec3 hitPos = hit.getLocation();

		this.level.sendParticles(
				new CustomTrailParticleOption(PortalCubedParticles.PORTAL_PROJECTILE, hitPos, settings.color(), 3),
				this.from.x, this.from.y, this.from.z, 1, 0, 0, 0, 0
		);

		if (hit.getType() != HitResult.Type.BLOCK || hit.isInside())
			return;

		PortalId id = new PortalId(pair.orElse(this.key), polarity);
		PortalPlacement placement = PortalBumper.findValidPlacement(id, this.level, hitPos, this.yRot, hit.getBlockPos(), hit.getDirection());
		if (placement == null)
			return;

		PortalValidator validator = new StandardPortalValidator(this.yRot);
		PortalData data = PortalData.createWithSettings(this.level, placement.pos(), placement.rotation(), validator, settings);
		this.level.portalManager().createPortal(id.key(), polarity, data);
	}

	public static BlockHitResult clip(ServerLevel level, Vec3 from, Vec3 lookAngle) {
		int range = level.getGameRules().getInt(PortalCubedGameRules.PORTAL_SHOT_RANGE_LIMIT);

		ClipContext ctx = new ClipContext(
				from.subtract(lookAngle.scale(MAGIC_OFFSET)),
				from.add(lookAngle.scale(range + MAGIC_OFFSET)),
				PortalShotClipContextMode.get(), ClipContext.Fluid.NONE, PortalCollisionContext.INSTANCE
		);
		ctx.pc$setIgnoreInteractionOverride(true);

		return level.clip(ctx);
	}
}
