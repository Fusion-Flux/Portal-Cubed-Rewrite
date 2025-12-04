package io.github.fusionflux.portalcubed.content.portal.manager.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;

public record SimplePortalStorage(Map<String, PortalPair> internal) implements PortalStorage {
	public static final Codec<SimplePortalStorage> CODEC = Codec.unboundedMap(Codec.STRING, PortalPair.CODEC).xmap(SimplePortalStorage::new, SimplePortalStorage::internal);

	public SimplePortalStorage() {
		this(new HashMap<>());
	}

	@Override
	@Nullable
	public PortalPair get(String key) {
		return this.internal.get(key);
	}

	@Override
	@Nullable
	public PortalPair put(String key, PortalPair value) {
		return this.internal.put(key, value);
	}

	@Override
	@Nullable
	public PortalPair remove(String key) {
		return this.internal.remove(key);
	}

	@Override
	@UnmodifiableView
	public Set<String> keys() {
		return Collections.unmodifiableSet(this.internal.keySet());
	}

	@Override
	@UnmodifiableView
	public Collection<PortalPair> values() {
		return Collections.unmodifiableCollection(this.internal.values());
	}

	@Override
	public void forEach(BiConsumer<String, PortalPair> consumer) {
		this.internal.forEach(consumer);
	}
}
