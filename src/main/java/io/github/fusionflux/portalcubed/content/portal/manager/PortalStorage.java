package io.github.fusionflux.portalcubed.content.portal.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.saveddata.SavedData;

public sealed interface PortalStorage permits PortalStorage.Simple, PortalStorage.Persistent {
	@Nullable
	PortalPair get(String key);

	default PortalPair getOrEmpty(String key) {
		PortalPair pair = this.get(key);
		return pair == null ? PortalPair.EMPTY : pair;
	}

	@Nullable
	PortalPair put(String key, PortalPair value);

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

	record Simple(Map<String, PortalPair> internal) implements PortalStorage {
		public static final Codec<Simple> CODEC = Codec.unboundedMap(Codec.STRING, PortalPair.CODEC).xmap(Simple::new, Simple::internal);

		public Simple() {
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
	}

	final class Persistent extends SavedData implements PortalStorage {
		public static final String ID = PortalCubed.id("portals").toString().replace(":", "_");

		private final Simple internal;

		public Persistent() {
			this.internal = new Simple();
		}

		@Override
		@Nullable
		public PortalPair get(String key) {
			return this.internal.get(key);
		}

		@Override
		@Nullable
		public PortalPair put(String key, PortalPair value) {
			this.setDirty();
			return this.internal.put(key, value);
		}

		@Override
		@Nullable
		public PortalPair remove(String key) {
			this.setDirty();
			return this.internal.remove(key);
		}

		@Override
		@UnmodifiableView
		public Set<String> keys() {
			return this.internal.keys();
		}

		@Override
		@UnmodifiableView
		public Collection<PortalPair> values() {
			return this.internal.values();
		}

		@Override
		public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
			RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
			Simple.CODEC.encodeStart(ops, this.internal).ifSuccess(result -> tag.merge((CompoundTag) result));
			return tag;
		}

		public static Persistent load(CompoundTag nbt, HolderLookup.Provider registries) {
			RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);

			Persistent persistent = new Persistent();
			Simple.CODEC.parse(ops, nbt).ifSuccess(persistent.internal::putAll);
			return persistent;
		}

		public static Factory<Persistent> factory() {
			return new Factory<>(
					Persistent::new,
					Persistent::load,
					null // FAPI makes this fine
			);
		}
	}
}
