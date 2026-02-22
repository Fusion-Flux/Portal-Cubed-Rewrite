package io.github.fusionflux.portalcubed.content.portal.ref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.ToDoubleFunction;

import org.jetbrains.annotations.Unmodifiable;

import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.transform.MultiPortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * A path through a series of portal pairs.
 */
public final class PortalPath {
	@Unmodifiable
	public final List<Entry> entries;
	public final AltEntries altEntries;
	public final double internalLength;

	private PortalPath(List<Entry> entries) {
		this.entries = entries;
		if (entries.isEmpty()) {
			throw new IllegalArgumentException("Cannot create an empty PortalPath");
		}

		List<AltEntries.Intermediate> intermediateAltEntries = new ArrayList<>();
		double length = 0;

		for (int i = 1; i <= entries.size() - 1; i++) {
			Entry firstEntry = entries.get(i - 1);
			Entry secondEntry = entries.get(i);
			AltEntries.Intermediate entry = new AltEntries.Intermediate(firstEntry.exited, secondEntry.entered);
			intermediateAltEntries.add(entry);
			length += entry.distance();
		}

		this.altEntries = new AltEntries(
				new AltEntries.Start(entries.getFirst().entered),
				Collections.unmodifiableList(intermediateAltEntries),
				new AltEntries.End(entries.getLast().exited)
		);

		this.internalLength = length;
	}

	public double length(Vec3 start, Vec3 end) {
		return Math.sqrt(this.lengthSqr(start, end));
	}

	public double length(Vec3 start, ToDoubleFunction<Vec3> endDistanceFunction) {
		ToDoubleFunction<Vec3> endDistanceSqrFunction = pos -> Mth.square(endDistanceFunction.applyAsDouble(pos));
		return Math.sqrt(this.lengthSqr(start, endDistanceSqrFunction));
	}

	public double lengthSqr(Vec3 start, Vec3 end) {
		return this.lengthSqr(start, end::distanceToSqr);
	}

	public double lengthSqr(Vec3 start, ToDoubleFunction<Vec3> endDistanceSqrFunction) {
		return Mth.square(this.internalLength)
				+ this.altEntries.start.entered.reference().get().origin().distanceToSqr(start)
				+ endDistanceSqrFunction.applyAsDouble(this.altEntries.end.exited.reference().get().origin());
	}

	/**
	 * @return a new {@link PortalTransform} that transforms across all portals in this path.
	 */
	public PortalTransform createTransform() {
		List<SinglePortalTransform> transforms = new ArrayList<>();

		for (Entry entry : this.entries) {
			transforms.add(new SinglePortalTransform(entry.entered.reference().get(), entry.exited.reference().get()));
		}

		return transforms.size() == 1 ? transforms.getFirst() : new MultiPortalTransform(transforms);
	}

	/**
	 * Create a new PortalPath that passes through the given portals at their centers, and then all portals in this path.
	 * @throws NoSuchElementException if the given portal is not linked
	 */
	public PortalPath prepend(PortalReference entered, PortalReference exited) {
		return this.prepend(HitPortal.ofCenter(entered), HitPortal.ofCenter(exited));
	}

	/**
	 * Create a new PortalPath that passes through the given portals, and then all portals in this path.
	 * @throws IllegalArgumentException if the given portals are not linked
	 */
	public PortalPath prepend(HitPortal entered, HitPortal exited) {
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(entered, exited));
		entries.addAll(this.entries);
		return new PortalPath(Collections.unmodifiableList(entries));
	}

	/**
	 * Associate this path with a value.
	 */
	public <T> With<T> with(T value) {
		return new With<>(this, value);
	}

	/**
	 * Create a new PortalPath that passes through the given portal at its center.
	 * @throws NoSuchElementException if the given portal is not linked
	 */
	public static PortalPath of(PortalReference entered) {
		return of(entered, entered.opposite().orElseThrow());
	}

	/**
	 * Create a new PortalPath that passes through the given portals at their centers.
	 * @throws IllegalArgumentException if the given portals are not linked
	 */
	public static PortalPath of(PortalReference entered, PortalReference exited) {
		return of(HitPortal.ofCenter(entered), HitPortal.ofCenter(exited));
	}

	/**
	 * Create a new PortalPath that passes through the given portals.
	 * @throws IllegalArgumentException if the given portals are not linked
	 */
	public static PortalPath of(HitPortal entered, HitPortal exited) {
		Entry entry = new Entry(entered, exited);
		return new PortalPath(List.of(entry));
	}

	/**
	 * Create a new PortalPath from the given list of {@link Entry entries}.
	 */
	public static PortalPath of(List<Entry> entries) {
		return new PortalPath(List.copyOf(entries));
	}

	/**
	 * An entry in a path, containing one linked pair of portals.
	 */
	public record Entry(HitPortal entered, HitPortal exited) {
		/**
		 * @throws IllegalArgumentException if the two portals are not linked
		 */
		public Entry {
			PortalId enteredId = entered.reference().id;
			PortalId exitedId = exited.reference().id;
			if (!enteredId.isOppositeOf(exitedId)) {
				throw new IllegalArgumentException("Portals must be linked: " + enteredId + " & " + exitedId);
			}
		}
	}

	/**
	 * An alternate view of a path's entries, allowing for easier use in some cases.
	 */
	public record AltEntries(Start start, @Unmodifiable List<Intermediate> intermediates, End end) {
		record Start(HitPortal entered) {}
		record End(HitPortal exited) {}

		record Intermediate(HitPortal exited, HitPortal entered) {
			public double distance() {
				return this.exited.reference().get().origin().distanceTo(this.entered.reference().get().origin());
			}
		}
	}

	/**
	 * A PortalPath, plus a value that was acquired at the end of it.
	 */
	public record With<T>(PortalPath path, T value) {
		public With<T> prepend(PortalReference entered) {
			return this.prepend(entered, entered.opposite().orElseThrow());
		}

		public With<T> prepend(PortalReference entered, PortalReference exited) {
			return new With<>(this.path.prepend(entered, exited), this.value);
		}
	}
}
