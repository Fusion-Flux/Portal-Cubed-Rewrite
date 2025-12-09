package io.github.fusionflux.portalcubed.content.portal.manager;

import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.PortalChangeListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class PortalSavedData extends SavedData implements PortalChangeListener {
	public static final String ID = PortalCubed.ID + "_portals";
	public static final Codec<Map<String, PortalPair>> PAIR_MAP_CODEC = Codec.unboundedMap(Codec.STRING, PortalPair.CODEC);

	private static final Logger logger = LogUtils.getLogger();

	public final ServerPortalManager manager;

	// fresh instance
	public PortalSavedData(ServerLevel level) {
		this.manager = new ServerPortalManager(level);
		this.manager.registerListener(this);
	}

	// loaded from data
	public PortalSavedData(ServerLevel level, CompoundTag nbt, HolderLookup.Provider registries) {
		this(level);

		RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);

		PAIR_MAP_CODEC.decode(ops, nbt)
				.ifSuccess(pair -> pair.getFirst().forEach(this.manager::setPair))
				.ifError(error -> logger.error("Failed to read portals: {}", error.message()));
	}

	@Override
	public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
		RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);

		PAIR_MAP_CODEC.encodeStart(ops, this.manager.pairs()).flatMap(
				tag -> tag instanceof CompoundTag compound ? DataResult.success(compound) : DataResult.error(() -> "Not a map")
		).ifSuccess(nbt::merge).ifError(error -> logger.error("Failed to save portals: {}", error.message()));

		return nbt;
	}

	@Override
	public void portalCreated(PortalReference reference) {
		this.setDirty();
	}

	@Override
	public void portalModified(Portal oldPortal, PortalReference reference) {
		this.setDirty();
	}

	@Override
	public void portalRemoved(PortalReference reference, Portal portal) {
		this.setDirty();
	}

	public static Factory<PortalSavedData> factory(ServerLevel level) {
		return new Factory<>(
				() -> new PortalSavedData(level),
				(nbt, registries) -> new PortalSavedData(level, nbt, registries),
				null // FAPI makes this fine
		);
	}
}
