package io.github.fusionflux.portalcubed.framework.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * A minimal collection where each entry is wrapped in a {@link WeakReference}.
 * This means that values will be removed automatically as they are garbage collected.
 */
public final class WeakCollection<T> {
	private final List<WeakReference<T>> references = new ArrayList<>();

	public void add(T entry) {
		if (entry == null) {
			throw new IllegalArgumentException("WeakQueue does not support null entries");
		}

		this.references.add(new WeakReference<>(entry));
	}

	public void forEach(Consumer<T> consumer) {
		for (Iterator<WeakReference<T>> itr = this.references.iterator(); itr.hasNext();) {
			WeakReference<T> reference = itr.next();
			T value = reference.get();
			if (value == null) {
				// reference freed
				itr.remove();
			} else {
				consumer.accept(value);
			}
		}
	}
}
