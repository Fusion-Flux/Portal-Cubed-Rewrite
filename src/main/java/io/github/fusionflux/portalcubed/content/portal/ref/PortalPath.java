package io.github.fusionflux.portalcubed.content.portal.ref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.ToDoubleFunction;

import org.jetbrains.annotations.Unmodifiable;

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
				+ this.altEntries.start.entered.get().data.origin().distanceToSqr(start)
				+ endDistanceSqrFunction.applyAsDouble(this.altEntries.end.exited.get().data.origin());
	}

	/**
	 * @return a new {@link PortalTransform} that transforms across all portals in this path.
	 */
	public PortalTransform createTransform() {
		List<SinglePortalTransform> transforms = new ArrayList<>();

		for (Entry entry : this.entries) {
			transforms.add(new SinglePortalTransform(entry.entered.get(), entry.exited.get()));
		}

		return transforms.size() == 1 ? transforms.getFirst() : new MultiPortalTransform(transforms);
	}

	/**
	 * Create a new PortalPath that passes through the given portal, and then all portals in this path.
	 * @throws NoSuchElementException if the given portal is not linked
	 */
	public PortalPath prepend(PortalReference entered) {
		return this.prepend(entered, entered.opposite().orElseThrow());
	}

	/**
	 * Create a new PortalPath that passes through the given portals, and then all portals in this path.
	 * @throws IllegalArgumentException if the given portals are not linked
	 */
	public PortalPath prepend(PortalReference entered, PortalReference exited) {
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(entered, exited));
		entries.addAll(this.entries);
		return new PortalPath(Collections.unmodifiableList(entries));
	}

	public <T> With<T> with(T value) {
		return new With<>(this, value);
	}

	/**
	 * Create a new PortalPath that passes through the given portal.
	 * @throws NoSuchElementException if the given portal is not linked
	 */
	public static PortalPath of(PortalReference entered) {
		return of(entered, entered.opposite().orElseThrow());
	}

	/**
	 * Create a new PortalPath that passes through the given portals.
	 * @throws IllegalArgumentException if the given portals are not linked
	 */
	public static PortalPath of(PortalReference entered, PortalReference exited) {
		Entry entry = new Entry(entered, exited);
		return new PortalPath(List.of(entry));
	}

	/**
	 * An entry in a path, containing one linked pair of portals.
	 */
	public record Entry(PortalReference entered, PortalReference exited) {
		/**
		 * @throws IllegalArgumentException if the two portals are not linked
		 */
		public Entry {
			if (!entered.id.isOppositeOf(exited.id)) {
				throw new IllegalArgumentException("Entered and exited portals must be linked");
			}
		}
	}

	/**
	 * An alternate view of a path's entries, allowing for easier use in some cases.
	 */
	public record AltEntries(Start start, @Unmodifiable List<Intermediate> intermediates, End end) {
		record Start(PortalReference entered) {}
		record End(PortalReference exited) {}

		record Intermediate(PortalReference exited, PortalReference entered) {
			public double distance() {
				return this.exited.get().data.origin().distanceTo(this.entered.get().data.origin());
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
