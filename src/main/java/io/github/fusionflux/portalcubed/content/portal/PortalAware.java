package io.github.fusionflux.portalcubed.content.portal;

import java.util.Iterator;
import java.util.function.Function;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.util.RecursiveIterator;
import net.minecraft.world.phys.Vec3;

/**
 * Some object that was acquired by passing through one or more portals.
 * @param <T> the type of value held by the tail of the chain. Null values are safe to use.
 */
public sealed interface PortalAware<T> extends Iterable<PortalAware<T>> {
	PortalReference enteredPortal();

	default PortalReference exitedPortal() {
		return this.enteredPortal().opposite().orElseThrow();
	}

	/**
	 * Find the tail at the end of this hit chain.
	 */
	Tail<T> findTail();

	/**
	 * @return a new {@link SinglePortalTransform} between the two portals of this step
	 */
	default SinglePortalTransform createSingleTransform() {
		return new SinglePortalTransform(this.enteredPortal().get(), this.exitedPortal().get());
	}

	/**
	 * @return a new {@link PortalTransform} that covers all crossed portals
	 */
	PortalTransform createFullTransform();

	/**
	 * Calculate the total distance between the given start and end points when passing through these portals.
	 * @param endFunction a function that will extract the end position from the final value
	 */
	default double calculateDistanceThroughCenters(Vec3 start, Function<T, Vec3> endFunction) {
		double distance = start.distanceTo(this.enteredPortal().get().data.origin());
		PortalAware<T> current = this;

		while (true) {
			switch (current) {
				case Mid<T> mid -> {
					PortalReference exited = mid.exitedPortal();
					PortalReference entered = mid.next().enteredPortal();
					distance += exited.get().data.origin().distanceTo(entered.get().data.origin());
					current = mid.next;
				}
				case Tail<T> tail -> {
					Vec3 end = endFunction.apply(tail.value());
					return distance + tail.exitedPortal().get().data.origin().distanceTo(end);
				}
			}
		}
	}

	@Override
	default Iterator<PortalAware<T>> iterator() {
		return new RecursiveIterator<>(this, aware -> switch (aware) {
			case Mid<T> mid -> mid.next();
			case Tail<T> ignored -> null;
		});
	}

	record Mid<T>(PortalReference enteredPortal, PortalAware<T> next) implements PortalAware<T> {
		@Override
		public Tail<T> findTail() {
			return this.next.findTail();
		}

		@Override
		public PortalTransform createFullTransform() {
			return this.createSingleTransform().andThen(this.next.createFullTransform());
		}
	}

	record Tail<T>(PortalReference enteredPortal, T value) implements PortalAware<T> {
		@Override
		public Tail<T> findTail() {
			return this;
		}

		@Override
		public PortalTransform createFullTransform() {
			return this.createSingleTransform();
		}
	}
}
