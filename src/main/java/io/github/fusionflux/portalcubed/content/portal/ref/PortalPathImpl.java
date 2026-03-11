package io.github.fusionflux.portalcubed.content.portal.ref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.transform.MultiPortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

final class PortalPathImpl implements PortalPath {
	private final List<Entry> entries;
	private final List<OffsetEntry> offsetEntries;
	private final double internalLength;

	@Nullable
	private PortalTransform transform;

	// assumes entries is immutable
	private PortalPathImpl(List<Entry> entries) {
		if (entries.isEmpty()) {
			throw new IllegalArgumentException("Cannot create an empty PortalPath");
		}

		this.entries = entries;

		List<OffsetEntry> offsetEntries = new ArrayList<>();
		double internalLength = 0;

		for (int i = 1; i <= entries.size() - 1; i++) {
			Entry firstEntry = entries.get(i - 1);
			Entry secondEntry = entries.get(i);
			OffsetEntry entry = new OffsetEntry(firstEntry.exited(), secondEntry.entered());
			offsetEntries.add(entry);
			internalLength += entry.distance();
		}

		this.offsetEntries = Collections.unmodifiableList(offsetEntries);
		this.internalLength = internalLength;
	}

	@Override
	public HitPortal first() {
		return this.entries.getFirst().entered();
	}

	@Override
	public HitPortal last() {
		return this.entries.getLast().exited();
	}

	@Override
	public Iterable<Entry> entries() {
		return this.entries;
	}

	@Override
	public Iterable<OffsetEntry> offsetEntries() {
		return this.offsetEntries;
	}

	@Override
	public double distanceThroughSqr(Vec3 start, ToDoubleFunction<Vec3> endDistanceSqrFunction) {
		return Mth.square(this.internalLength)
				+ this.first().pos().distanceToSqr(start)
				+ endDistanceSqrFunction.applyAsDouble(this.last().pos());
	}

	@Override
	public PortalTransform transform() {
		if (this.transform == null) {
			this.transform = this.createTransform();
		}

		return this.transform;
	}

	private PortalTransform createTransform() {
		if (this.entries.size() == 1) {
			return this.entries.getFirst().createTransform();
		}

		List<SinglePortalTransform> transforms = new ArrayList<>();
		this.entries.forEach(entry -> transforms.add(entry.createTransform()));
		return new MultiPortalTransform(transforms);
	}

	@Override
	public PortalPathImpl prepend(PortalReference entered, PortalReference exited) {
		return this.prepend(HitPortal.ofCenter(entered), HitPortal.ofCenter(exited));
	}

	@Override
	public PortalPathImpl prepend(HitPortal entered, HitPortal exited) {
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(entered, exited));
		entries.addAll(this.entries);
		return new PortalPathImpl(Collections.unmodifiableList(entries));
	}

	@Override
	public <T> With<T> with(T value) {
		return new With<>(this, value);
	}

	@Override
	public Serialized serialize() {
		return SerializedPortalPathImpl.of(this);
	}

	static PortalPath ofTrusted(List<Entry> entries) {
		return new PortalPathImpl(entries);
	}

	static PortalPath ofUntrusted(List<Entry> entries) {
		return new PortalPathImpl(List.copyOf(entries));
	}
}
