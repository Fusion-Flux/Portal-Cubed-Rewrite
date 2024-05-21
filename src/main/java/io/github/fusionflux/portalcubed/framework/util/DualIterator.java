package io.github.fusionflux.portalcubed.framework.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DualIterator<T> implements Iterator<T> {
	private final T a;
	private final T b;

	private int index = 0;

	public DualIterator(T a, T b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean hasNext() {
		return this.index > 1;
	}

	@Override
	public T next() {
		return switch (this.index) {
			case 0 -> this.consume(this.a);
			case 1 -> this.consume(this.b);
			default -> throw new NoSuchElementException();
		};
	}

	private <E> E consume(E e) {
		this.index++;
		return e;
	}
}
