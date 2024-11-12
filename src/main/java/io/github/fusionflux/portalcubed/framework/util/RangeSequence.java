package io.github.fusionflux.portalcubed.framework.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;

public final class RangeSequence<T> implements Iterable<RangeSequence.Entry<T>> {
	private final float min;
	private final float max;
	private final List<Entry<T>> entries;

	private RangeSequence(float min, float max, List<Entry<T>> entries) {
		this.min = min;
		this.max = max;
		this.entries = entries;
	}

	public T get(float progress) {
		return this.getEntry(progress).value;
	}

	public Entry<T> getEntry(float progress) {
		if (progress < this.min) {
			return this.entries.get(0);
		} else if (progress >= this.max) {
			return this.entries.get(this.entries.size() - 1);
		} else {
			for (Entry<T> entry : this.entries) {
				if (progress < entry.max) {
					return entry;
				}
			}
		}

		throw new IllegalStateException("Malformed RangeSequence: " + this);
	}

	@Override
	public Iterator<Entry<T>> iterator() {
		return this.entries.iterator();
	}

	public void toNetwork(FriendlyByteBuf buf, BiConsumer<T, FriendlyByteBuf> serializer) {
		buf.writeFloat(this.min);
		buf.writeVarInt(this.entries.size());
		for (Entry<T> entry : this.entries) {
			buf.writeFloat(entry.max);
			serializer.accept(entry.value, buf);
		}
	}

	public static <T> RangeSequence<T> fromNetwork(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> deserializer) {
		Builder<T> builder = start(buf.readFloat());
		int entries = buf.readVarInt();
		for (int i = 0; i < entries; i++) {
			float max = buf.readFloat();
			T value = deserializer.apply(buf);
			builder.until(max, value);
		}
		return builder.build();
	}

	@Override
	public String toString() {
		return "[%f, %f) | %s".formatted(this.min, this.max, this.entries);
	}

	public static <T> Builder<T> start(float min) {
		return new Builder<>(min);
	}

	public record Entry<T>(float min, float max, T value) {
		@Override
		public String toString() {
			return "<%f: %s".formatted(this.max, this.value);
		}
	}

	public static class Builder<T> {
		private final float min;
		private final List<Entry<T>> entries;

		private float prevMax;

		private Builder(float min) {
			this.min = min;
			this.prevMax = min;
			this.entries = new ArrayList<>();
		}

		public Builder<T> until(double max, T value) {
			return this.until((float) max, value);
		}

		public Builder<T> until(float max, T value) {
			if (max <= this.prevMax) {
				throw new IllegalArgumentException("Cannot add a range with lower max than previous: " + max + " (prev: " + this.prevMax + ')');
			}
			this.entries.add(new Entry<>(this.prevMax, max, value));
			this.prevMax = max;
			return this;
		}

		public RangeSequence<T> build() {
			if (this.entries.isEmpty()) {
				throw new IllegalStateException("Builder is empty");
			}
			float max = this.entries.get(this.entries.size() - 1).max;
			return new RangeSequence<>(this.min, max, this.entries);
		}
	}
}
