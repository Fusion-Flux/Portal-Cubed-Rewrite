package io.github.fusionflux.portalcubed.content.misc;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;

import net.minecraft.client.player.Input;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.quiltmc.loader.api.minecraft.ClientOnly;

/*
 * Source-like physics:
 * - no air drag
 * - soft air speed limit of 30u/s (about half a block)
 * - speed limit is full vector length, not components
 * - input is ignored when the projection of velocity onto acceleration is above the limit
 * Reference: https://steamcommunity.com/sharedfiles/filedetails/?id=184184420
 */
public class SourcePhysics {
	// // 128 in a 2x2 panel
	public static final double BLOCKS_PER_UNIT = 1 / 64f;
	public static final double SPEED_LIMIT_UNITS = 30;
	public static final double SPEED_LIMIT_BLOCKS = BLOCKS_PER_UNIT * SPEED_LIMIT_UNITS;
	public static final double SPEED_LIMIT = SPEED_LIMIT_BLOCKS / 20; // 20 tps

	public static boolean appliesTo(Player player) {
		return player.getItemBySlot(EquipmentSlot.FEET).is(PortalCubedItemTags.APPLY_SOURCE_PHYSICS)
				&& !player.onGround()
				&& !player.getAbilities().flying;
	}

	public static float getAirDrag(LivingEntity entity, float original) {
		return entity instanceof Player player && appliesTo(player) ? 1 : original;
	}

	@ClientOnly
	public static void applyInput(Player player, Input input) {
		if (!appliesTo(player))
			return;

		Vec3 vel = player.getDeltaMovement();
		Vec3 inputVec = new Vec3(input.leftImpulse, 0, input.forwardImpulse);
		double projection = projectionMagnitude(vel, inputVec);
		if (projection > SPEED_LIMIT) {
			// too fast, discard
			// don't use 0, will stop sprinting
			input.leftImpulse = 1.0E-4f;
			input.forwardImpulse = 1.0E-4f;
		}
	}

	public static double projectionMagnitude(Vec3 a, Vec3 b) {
		double angle = angleBetween(a, b);
		return a.length() * Math.cos(angle);
	}

	public static double angleBetween(Vec3 a, Vec3 b) {
		return a.dot(b) / (a.length() * b.length());
	}
}
