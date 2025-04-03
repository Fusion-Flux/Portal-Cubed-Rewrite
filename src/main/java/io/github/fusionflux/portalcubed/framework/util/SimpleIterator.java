package io.github.fusionflux.portalcubed.framework.util;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.AbstractIterator;

public final class SimpleIterator<T> extends AbstractIterator<T> {
	private final Generator<T> generator;

	private int i = 0;

	private SimpleIterator(Generator<T> generator) {
		this.generator = generator;
	}

	@Override
	protected T computeNext() {
		T generated = this.generator.generate(this.i);
		if (generated == null) {
			return this.endOfData();
		}

		this.i++;
		return generated;
	}

	public static <T> Iterator<T> create(Generator<T> generator) {
		return new SimpleIterator<>(generator);
	}

	@FunctionalInterface
	public interface Generator<T> {
		@Nullable
		T generate(int i);
	}
}
