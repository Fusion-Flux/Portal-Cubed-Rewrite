package io.github.fusionflux.portalcubed.framework.util;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

/// To be replaced once we update to 26.1
public final class ScopedValueAtHome<T> {
	private final ThreadLocal<@Nullable T> threadLocal = new ThreadLocal<>();

	public T get() throws NoSuchElementException {
		return this.orElseThrow(() -> new NoSuchElementException("No value present"));
	}

	public boolean isBound() {
		return this.threadLocal.get() != null;
	}

	public T orElse(T other) {
		T value = this.threadLocal.get();
		return value == null ? other : value;
	}

	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		T value = this.threadLocal.get();
		if (value != null)
			return value;

		throw exceptionSupplier.get();
	}

	public void runWhere(T value, Runnable runnable) {
		this.set(value);
		runnable.run();
		this.set(null);
	}

	private void set(T value) {
		this.threadLocal.set(value);
	}

	public static <T> ScopedValueAtHome<T> newInstance() {
		return new ScopedValueAtHome<>();
	}
}
