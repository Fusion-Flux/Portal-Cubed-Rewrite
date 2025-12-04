package io.github.fusionflux.portalcubed.content.portal.gun;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

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
import io.github.fusionflux.portalcubed.content.portal.placement.validator.NonePortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.StandardPortalValidator;
import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticleOption;
import io.github.fusionflux.portalcubed.framework.util.Angle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record PortalGunShootContext(
		Optional<String> key,
		ServerLevel level,
		Vec3 from,
		Vec3 lookAngle,
		float yRot
) {
	// if this value is too high, clipping becomes noticeably imprecise, hitting blocks it shouldn't
	public static final int MAX_CLIP_STEP = 32;

	public PortalGunShootContext(@Nullable String key, ServerLevel level, Vec3 from, Vec3 lookAngle, float yRot) {
		this(Optional.ofNullable(key), level, from, lookAngle, yRot);
	}

	public void shootAndPlace(Optional<String> pair, Polarity polarity, PortalSettings settings) {
		String key = pair.or(this::key).orElse(null);
		if (key == null)
			return;

		PortalId id = new PortalId(key, polarity);
		PortalShot shot = this.shoot(id);

		int color = settings.color().getOpaque(this.level.getGameTime());
		this.level.sendParticles(
				new CustomTrailParticleOption(PortalCubedParticles.PORTAL_PROJECTILE, shot.hit.getLocation(), color, 3),
				this.from.x, this.from.y, this.from.z, 1, 0, 0, 0, 0
		);

		PortalPlacement placement = shot.placement;
		if (placement == null)
			return;

		PortalValidator validator = settings.validate() ? new StandardPortalValidator(placement.rotationAngle()) : NonePortalValidator.INSTANCE;
		PortalData data = PortalData.createWithSettings(this.level, placement.pos(), placement.rotation(), validator, settings);
		this.level.portalManager().createPortal(id.key(), polarity, data);
	}

	public PortalShot shoot(@Nullable PortalId ignored) {
		BlockHitResult hit = clip(this.level, this.from, this.lookAngle);
		if (hit.getType() != HitResult.Type.BLOCK || hit.isInside())
			return new PortalShot(null, hit);

		Direction face = hit.getDirection();
		Angle bias = this.getBias(face);

		PortalPlacement placement = PortalBumper.findValidPlacement(ignored, this.level, hit.getLocation(), this.yRot, hit.getBlockPos(), face, bias, null);
		return new PortalShot(placement, hit);
	}

	@Nullable
	private Angle getBias(Direction face) {
		if (!face.getAxis().isHorizontal())
			return null;

		Direction left = face.getClockWise();
		double dot = this.lookAngle.dot(left.getUnitVec3());
		return dot > 0 ? Angle.R270 : Angle.R90;
	}

	public static BlockHitResult clip(ServerLevel level, Vec3 from, Vec3 lookAngle) {
		int range = level.getGameRules().getInt(PortalCubedGameRules.PORTAL_SHOT_RANGE_LIMIT);

		double distance = range;
		while (distance > 0) {
			double step = Math.min(distance, MAX_CLIP_STEP);
			BlockHitResult result = clip(level, from, lookAngle, step);
			if (result.getType() == HitResult.Type.BLOCK)
				return result;

			distance -= step;
			from = result.getLocation();
		}

		// no hit. create a miss result at the maximum distance
		Vec3 end = from.add(lookAngle.scale(range));
		Direction nearestDirection = Direction.getApproximateNearest(lookAngle);
		BlockPos endPos = BlockPos.containing(end);
		return BlockHitResult.miss(end, nearestDirection, endPos);
	}

	private static BlockHitResult clip(ServerLevel level, Vec3 from, Vec3 lookAngle, double distance) {
		Vec3 to = from.add(lookAngle.scale(distance));

		ClipContext ctx = new ClipContext(from, to, PortalShotClipContextMode.get(), ClipContext.Fluid.NONE, PortalCollisionContext.INSTANCE);
		ctx.pc$setIgnoreInteractionOverride(true);

		return level.clip(ctx);
	}

	public static PortalGunShootContext ofPlayer(ServerPlayer player) {
		return new PortalGunShootContext(
				Optional.of(player.getGameProfile().getName()), player.serverLevel(),
				player.getEyePosition(), player.getLookAngle(), player.getYRot()
		);
	}

	public record PortalShot(@Nullable PortalPlacement placement, BlockHitResult hit) {
	}
}
