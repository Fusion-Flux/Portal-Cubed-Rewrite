package io.github.fusionflux.portalcubed.content.portal.clip;

import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Represents the result of a raycast that only interacts with portals.
 * <p>
 * PortalHitResults are chained, since a raycast can pass through multiple portals.
 * Each entry in the chain will be an {@link Mid intermediate instance},
 * except for the final one, which will be a {@link Tail tail} instance.
 */
public sealed interface PortalHitResult {
	PortalReference enteredPortal();

	/**
	 * The position on the entered portal that was hit by the raycast.
	 */
	Vec3 hit();

	default PortalReference exitedPortal() {
		return this.enteredPortal().opposite().orElseThrow();
	}

	/**
	 * The position on the exited portal that corresponds to the hit on the entered portal.
	 */
	Vec3 exitHit();

	/**
	 * Invoke the given consumer with each entry in the hit chain.
	 */
	void forEach(Consumer<PortalHitResult> consumer);

	/**
	 * Find the tail at the end of this hit chain.
	 */
	Tail findTail();

	/**
	 * @return true if this result's location is farther away from {@code pos} than the given {@link HitResult}
	 */
	default boolean isFartherThan(HitResult hit, Vec3 pos) {
		double vanillaDist = hit.getLocation().distanceToSqr(pos);
		double thisDist = this.hit().distanceToSqr(pos);
		// bias away from vanilla slightly, since raycast logic differs slightly between blocks and portals
		return vanillaDist + 1e-5 < thisDist;
	}

	/**
	 * A single step in a raycast that passed through more than one pair of portals.
	 */
	record Mid(PortalReference enteredPortal, Vec3 hit, Vec3 exitHit, PortalHitResult next) implements PortalHitResult {
		@Override
		public void forEach(Consumer<PortalHitResult> consumer) {
			consumer.accept(this);
			this.next.forEach(consumer);
		}

		@Override
		public Tail findTail() {
			return this.next.findTail();
		}
	}

	/**
	 * Either the result of a raycast that only passed through one pair of portals, or the tail of a chain of teleports.
	 */
	record Tail(PortalReference enteredPortal, Vec3 hit, Vec3 exitHit, Vec3 end) implements PortalHitResult {
		@Override
		public void forEach(Consumer<PortalHitResult> consumer) {
			consumer.accept(this);
		}

		@Override
		public Tail findTail() {
			return this;
		}
	}
}
