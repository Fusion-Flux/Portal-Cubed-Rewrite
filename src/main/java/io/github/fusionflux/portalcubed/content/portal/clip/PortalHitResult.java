package io.github.fusionflux.portalcubed.content.portal.clip;

import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import net.minecraft.world.phys.Vec3;

/**
 * Represents the result of a raycast that only interacts with portals.
 */
public sealed interface PortalHitResult permits PortalHitResult.Closed, PortalHitResult.Open {
	PortalReference enteredPortal();

	Vec3 hit();

	/**
	 * The result of a raycast that hit a closed portal.
	 */
	record Closed(PortalReference enteredPortal, Vec3 hit) implements PortalHitResult {
	}

	/**
	 * Sub-interface for raycasts that hit open portals.
	 */
	sealed interface Open extends PortalHitResult {
		Vec3 exitHit();

		default PortalReference exitedPortal() {
			return this.enteredPortal().opposite().orElseThrow();
		}

		void forEach(Consumer<Open> consumer);
	}

	/**
	 * A single step in a raycast that passed through more than one pair of portals.
	 */
	record Mid(PortalReference enteredPortal, Vec3 hit, Vec3 exitHit, PortalHitResult next) implements Open {
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
	record Tail(PortalReference enteredPortal, Vec3 hit, Vec3 exitHit, Vec3 end) implements Open {
		@Override
		public void forEach(Consumer<Open> consumer) {
			consumer.accept(this);
		}
	}
}
