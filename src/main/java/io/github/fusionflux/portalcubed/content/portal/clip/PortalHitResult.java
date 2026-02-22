package io.github.fusionflux.portalcubed.content.portal.clip;

import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
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
	PortalReference hitPortal();

	/**
	 * The position on the entered portal that was hit by the raycast.
	 */
	Vec3 hit();

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
	 * A PortalHitResult that hit an open portal, passing through it.
	 */
	sealed interface Open extends PortalHitResult permits Mid, Tail.Open {
		default PortalReference exitedPortal() {
			return this.hitPortal().opposite().orElseThrow();
		}

		/**
		 * The position on the exited portal that corresponds to the hit on the entered portal.
		 */
		Vec3 exitHit();
	}

	/**
	 * A single step in a raycast that passed through more than one pair of portals.
	 */
	record Mid(PortalReference hitPortal, Vec3 hit, Vec3 exitHit, PortalHitResult next) implements Open {
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
	 * The end of a chain of portal hits. may be either open or closed.
	 */
	sealed interface Tail extends PortalHitResult {
		@Override
		default void forEach(Consumer<PortalHitResult> consumer) {
			consumer.accept(this);
		}

		@Override
		default Tail findTail() {
			return this;
		}

		record Open(PortalReference hitPortal, Vec3 hit, Vec3 exitHit, Vec3 end) implements Tail, PortalHitResult.Open {}
		record Closed(PortalReference hitPortal, Vec3 hit) implements Tail {}
	}
}
