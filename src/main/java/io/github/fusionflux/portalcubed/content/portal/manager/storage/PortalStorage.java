package io.github.fusionflux.portalcubed.content.portal.manager.storage;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;

/**
 * Stores portal pairs, either transiently or persistently.
 */
public sealed interface PortalStorage permits SimplePortalStorage, PersistentPortalStorage {
	@Nullable
	PortalPair get(String key);

	@ApiStatus.NonExtendable
	default PortalPair getOrEmpty(String key) {
		PortalPair pair = this.get(key);
		return pair == null ? PortalPair.EMPTY : pair;
	}

	@Nullable
	PortalPair put(String key, PortalPair value);

	@ApiStatus.NonExtendable
	default void putAll(PortalStorage other) {
		for (String key : other.keys()) {
			this.put(key, other.get(key));
		}
	}

	@Nullable
	PortalPair remove(String key);

	@UnmodifiableView
	Set<String> keys();

	@UnmodifiableView
	Collection<PortalPair> values();

	void forEach(BiConsumer<String, PortalPair> consumer);
}
