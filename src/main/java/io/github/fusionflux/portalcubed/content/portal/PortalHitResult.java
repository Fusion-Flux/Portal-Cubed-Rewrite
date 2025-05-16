package io.github.fusionflux.portalcubed.content.portal;

import java.util.function.Consumer;

import net.minecraft.world.phys.Vec3;

/**
 * Represents the result of a raycast that only interacts with portals.
 */
public sealed interface PortalHitResult permits PortalHitResult.Closed, PortalHitResult.Open {
	PortalInstance.Holder enteredPortal();

	default PortalPair.Holder pair() {
		return this.enteredPortal().pair();
	}

	Vec3 hit();

	/**
	 * The result of a raycast that hit a closed portal.
	 */
	record Closed(PortalInstance.Holder enteredPortal, Vec3 hit) implements PortalHitResult {
	}

	/**
	 * Sub-interface for raycasts that hit open portals.
	 */
	sealed interface Open extends PortalHitResult {
		Vec3 exitHit();

		default PortalInstance.Holder exitedPortal() {
			return this.enteredPortal().opposite().orElseThrow();
		}

		void forEach(Consumer<Open> consumer);
	}

	/**
	 * A single step in a raycast that passed through more than one pair of portals.
	 */
	record Mid(PortalInstance.Holder enteredPortal, Vec3 hit, Vec3 exitHit, PortalHitResult next) implements Open {
		@Override
		public void forEach(Consumer<Open> consumer) {
			consumer.accept(this);
			if (this.next instanceof Open open) {
				open.forEach(consumer);
			}
		}
	}

	/**
	 * Either the result of a raycast that only passed through one pair of portals, or the tail of a chain of teleports.
	 */
	record Tail(PortalInstance.Holder enteredPortal, Vec3 hit, Vec3 exitHit, Vec3 end) implements Open {
		@Override
		public void forEach(Consumer<Open> consumer) {
			consumer.accept(this);
		}
	}
}
