package io.github.fusionflux.portalcubed.content.portal.manager.storage;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.saveddata.SavedData;

public final class PersistentPortalStorage extends SavedData implements PortalStorage {
	public static final String ID = PortalCubed.id("portals").toString().replace(":", "_");

	private final SimplePortalStorage internal;

	public PersistentPortalStorage() {
		this.internal = new SimplePortalStorage();
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
	public void forEach(BiConsumer<String, PortalPair> consumer) {
		this.internal.forEach(consumer);
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
		SimplePortalStorage.CODEC.encodeStart(ops, this.internal).ifSuccess(result -> tag.merge((CompoundTag) result));
		return tag;
	}

	public static PersistentPortalStorage load(CompoundTag nbt, HolderLookup.Provider registries) {
		RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);

		PersistentPortalStorage persistent = new PersistentPortalStorage();
		SimplePortalStorage.CODEC.parse(ops, nbt).ifSuccess(persistent.internal::putAll);
		return persistent;
	}

	public static Factory<PersistentPortalStorage> factory() {
		return new Factory<>(
				PersistentPortalStorage::new,
				PersistentPortalStorage::load,
				null // FAPI makes this fine
		);
	}
}
