package io.github.fusionflux.portalcubed.content.portal.command.argument.portal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Iterators;

import io.github.fusionflux.portalcubed.content.portal.PortalData;

public final class PortalAttributeMap implements Iterable<PortalAttributeMap.Entry<?>> {
	private final Map<PortalAttribute<?>, Object> map = new HashMap<>();

	public <T> void put(PortalAttribute<T> attribute, T value) {
		this.map.put(attribute, value);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T get(PortalAttribute<T> attribute) {
		return (T) this.map.get(attribute);
	}

	public boolean containsKey(PortalAttribute<?> attribute) {
		return this.map.containsKey(attribute);
	}

	@NotNull
	@Override
	public Iterator<Entry<?>> iterator() {
		return Iterators.transform(this.map.keySet().iterator(), this::getEntry);
	}

	public PortalData modify(PortalData data) {
		for (Entry<?> entry : this) {
			data = entry.modify(data);
		}

		return data;
	}

	private <T> Entry<T> getEntry(PortalAttribute<T> attribute) {
		T value = this.get(attribute);
		Objects.requireNonNull(value, () -> "Missing value for key: " + attribute);
		return new Entry<>(attribute, value);
	}

	public record Entry<T>(PortalAttribute<T> attribute, T value) {
		public PortalData modify(PortalData data) {
			return this.attribute.modify(data, this.value);
		}
	}
}
