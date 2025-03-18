package io.github.fusionflux.portalcubed.content.portal.renderer;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public record RecursionAttachedResource<T>(Supplier<T> factory, ObjectArrayList<T> recursions) {
	private static final List<WeakReference<RecursionAttachedResource<?>>> ALL = new LinkedList<>();

	public void ensure(int recursionIndex) {
		for (int i = this.recursions.size(); i <= recursionIndex; i++) {
			this.recursions.add(this.factory.get());
		}
	}

	public T get() {
		int recursionIndex = PortalRenderer.recursion() - 1;
		this.ensure(recursionIndex);
		return this.recursions.get(recursionIndex);
	}

	public void set(@NotNull T value) {
		int recursionIndex = PortalRenderer.recursion() - 1;
		this.ensure(recursionIndex);
		this.recursions.set(recursionIndex, value);
	}

	public static void cleanup() {
		// Clear the lists and remove invalid references, let the GC handle everything else
		ALL.removeIf(ref -> {
			RecursionAttachedResource<?> r = ref.get();
			if (r == null)
				return true;
			r.recursions.clear();
			return false;
		});
	}

	public static <T> RecursionAttachedResource<T> create(Supplier<T> factory) {
		RecursionAttachedResource<T> r = new RecursionAttachedResource<>(factory, new ObjectArrayList<>());
		ALL.add(new WeakReference<>(r));
		return r;
	}
}
