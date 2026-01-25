package io.github.fusionflux.portalcubed.framework.util;

import java.util.Iterator;
import java.util.function.UnaryOperator;

public final class RecursiveIterator<T> implements Iterator<T> {
	private final UnaryOperator<T> nextFunction;
	private T current;

	public RecursiveIterator(T initial, UnaryOperator<T> nextFunction) {
		this.nextFunction = nextFunction;
		this.current = initial;
	}

	@Override
	public boolean hasNext() {
		return this.current != null;
	}

	@Override
	public T next() {
		T value = this.current;
		this.current = this.nextFunction.apply(value);
		return value;
	}
}
