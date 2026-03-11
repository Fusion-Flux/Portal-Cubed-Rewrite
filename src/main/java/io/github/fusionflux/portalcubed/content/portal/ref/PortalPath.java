package io.github.fusionflux.portalcubed.content.portal.ref;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

import com.mojang.serialization.DataResult;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/// Represents a path through a series of linked portal pairs. Always contains at least one pair of portals.
public sealed interface PortalPath permits PortalPathImpl {
	/// @return the first portal that was entered
	HitPortal first();

	/// @return the last portal that was exited
	HitPortal last();

	Iterable<Entry> entries();

	Iterable<OffsetEntry> offsetEntries();

	/// @return the squared distance between the two given points, when passing through this path
	default double distanceThroughSqr(Vec3 start, Vec3 end) {
		return this.distanceThroughSqr(start, end::distanceToSqr);
	}

	/// A variant of [#length(Vec3, Vec3)] that allows for providing a function as the end goal instead of a single point.
	///
	/// This allows for finding the distance to other objects, such as the distance between a point and an [AABB].
	double distanceThroughSqr(Vec3 start, ToDoubleFunction<Vec3> endDistanceSqrFunction);

	default double distanceThrough(Vec3 start, Vec3 end) {
		return this.distanceThrough(start, end::distanceTo);
	}

	default double distanceThrough(Vec3 start, ToDoubleFunction<Vec3> endDistanceFunction) {
		ToDoubleFunction<Vec3> endDistanceSqrFunction = pos -> Mth.square(endDistanceFunction.applyAsDouble(pos));
		return Math.sqrt(this.distanceThroughSqr(start, endDistanceSqrFunction));
	}

	/// @return a [PortalTransform] that transforms across all portals in this path.
	PortalTransform transform();

	/// Create a new PortalPath that passes through the given portals at their centers, and then all portals in this path.
	/// @throws NoSuchElementException if the given portal is not linked
	PortalPath prepend(PortalReference entered, PortalReference exited);

	/// Create a new PortalPath that passes through the given portals, and then all portals in this path.
	/// @throws IllegalArgumentException if the given portals are not linked
	PortalPath prepend(HitPortal entered, HitPortal exited);

	/// Associate this path with a value.
	<T> With<T> with(T value);

	/// Convert this path into a serializable format.
	Serialized serialize();

	/// Create a new PortalPath that passes through the given portal at its center.
	/// @throws NoSuchElementException if the given portal is not linked
	static PortalPath of(PortalReference entered) {
		return of(entered, entered.oppositeOrThrow());
	}

	/// Create a new PortalPath that passes through the given portals at their centers.
	/// @throws IllegalArgumentException if the given portals are not linked
	static PortalPath of(PortalReference entered, PortalReference exited) {
		return of(HitPortal.ofCenter(entered), HitPortal.ofCenter(exited));
	}

	/// Create a new PortalPath that passes through the given portals.
	/// @throws IllegalArgumentException if the given portals are not linked
	static PortalPath of(HitPortal entered, HitPortal exited) {
		Entry entry = new Entry(entered, exited);
		return PortalPathImpl.ofTrusted(List.of(entry));
	}

	/// Create a new PortalPath from the given list of entries.
	/// @throws IllegalArgumentException if the given list is empty
	static PortalPath of(List<Entry> entries) {
		return PortalPathImpl.ofUntrusted(entries);
	}

	/// Attempt to create a new PortalPath from the given list of entries.
	/// @return the created path, or empty if the list is empty
	static Optional<PortalPath> optionalOf(List<Entry> entries) {
		return entries.isEmpty() ? Optional.empty() : Optional.of(of(entries));
	}

	/// An entry in a path, containing one linked pair of portals.
	record Entry(HitPortal entered, HitPortal exited) {
		/// @throws IllegalArgumentException if the two portals are not linked
		public Entry {
			assertLinked(entered, exited);
		}

		/// @return a new [SinglePortalTransform] between this entry's pair of portals
		public SinglePortalTransform createTransform() {
			Portal entered = this.entered.reference().get();
			Portal exited = this.exited.reference().get();
			return new SinglePortalTransform(entered, exited);
		}
	}

	/// An alternate way of representing the entries in a path.
	/// Instead of iterating the linked pairs, the gaps between linked pairs are iterated.
	///
	/// This is useful in some cases alongside [#first()] and [#last()].
	record OffsetEntry(HitPortal exited, HitPortal entered) {
		/// @return the distance between the hit points on the entered and exited portals
		public double distance() {
			Vec3 from = this.exited.reference().get().origin();
			Vec3 to = this.entered.reference().get().origin();
			return from.distanceTo(to);
		}
	}

	/// A PortalPath, plus a value that was found at the end of it.
	record With<T>(PortalPath path, T value) {
		public With<T> prepend(PortalReference entered) {
			return this.prepend(entered, entered.oppositeOrThrow());
		}

		public With<T> prepend(PortalReference entered, PortalReference exited) {
			return new With<>(this.path.prepend(entered, exited), this.value);
		}
	}

	/// A [PortalPath] that is ready for serialization.
	/// Must be resolved against a [PortalManager] to be used.
	sealed interface Serialized permits SerializedPortalPathImpl {
		StreamCodec<ByteBuf, Serialized> STREAM_CODEC = SerializedPortalPathImpl.STREAM_CODEC;

		/// Attempt to resolve this into a full [PortalPath].
		/// May fail if any referenced portals are missing.
		DataResult<PortalPath> resolve(PortalManager manager);
	}

	private static void assertLinked(HitPortal first, HitPortal second) {
		PortalId firstId = first.reference().id;
		PortalId secondId = second.reference().id;
		if (!firstId.isOppositeOf(secondId)) {
			throw new IllegalArgumentException("Portals must be linked: " + firstId + " & " + secondId);
		}
	}
}
