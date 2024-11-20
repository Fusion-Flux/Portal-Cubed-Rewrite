package io.github.fusionflux.portalcubed.content.portal.manager;

import java.util.HashSet;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;

import io.github.fusionflux.portalcubed.PortalCubed;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;

import io.github.fusionflux.portalcubed.content.portal.PortalData;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalType;

import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.UpdatePortalPairPacket;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import org.quiltmc.qsl.networking.api.PlayerLookup;

public class ServerPortalManager extends PortalManager {
	public final ServerLevel level;

	public ServerPortalManager(ServerLevel level) {
		super(level);
		this.level = level;
	}

	/**
	 * Create a new portal.
	 * If the pair does not exist, a new one is created.
	 * If an old portal already exists, it will be removed.
	 */
	public void createPortal(UUID pairId, PortalType type, PortalData data) {
		this.modifyPair(pairId, pair -> pair.with(type, new PortalInstance(data)));
	}

	@Override
	public void setPair(UUID id, @Nullable PortalPair pair) {
		super.setPair(id, pair);
		UpdatePortalPairPacket packet = new UpdatePortalPairPacket(id, pair);
		PortalCubedPackets.sendToClients(PlayerLookup.world(this.level), packet);
	}

	public void removePortalsInBox(AABB bounds) {
		// TODO: this is a mess. Need a section-based lookup and easy removal
		// copy the ID set to avoid a CME
		for (UUID key : new HashSet<>(this.getAllIds())) {
			this.modifyPair(key, pair -> {
				if (pair.primary().isPresent()) {
					PortalInstance primary = pair.primary().get();
					if (primary.renderBounds.intersects(bounds)) {
						pair = pair.without(PortalType.PRIMARY);
					}
				}
				if (pair.secondary().isPresent()) {
					PortalInstance secondary = pair.secondary().get();
					if (secondary.renderBounds.intersects(bounds)) {
						pair = pair.without(PortalType.SECONDARY);
					}
				}

				return pair;
			});
		}
	}

	public CompoundTag save(CompoundTag nbt) {
		nbt.put("portals", PORTALS_CODEC.encodeStart(NbtOps.INSTANCE, this.portals).result().orElseThrow());
		return nbt;
	}

	public void load(CompoundTag nbt) {
		PORTALS_CODEC.decode(NbtOps.INSTANCE, nbt.get("portals")).result().map(Pair::getFirst).ifPresent(map -> {
			this.portals.clear();
			this.portals.putAll(map);
		});
	}

	public static class PersistentState extends SavedData {
		public static final String ID = PortalCubed.id("portals").toString();
		public final ServerPortalManager manager;

		private PersistentState(ServerLevel level) {
			this.manager = new ServerPortalManager(level);
		}

		private PersistentState(ServerLevel level, CompoundTag nbt) {
			this(level);
			this.manager.load(nbt);
		}

		@Override
		@NotNull
		public CompoundTag save(CompoundTag nbt) {
			return this.manager.save(nbt);
		}

		public static Factory<PersistentState> factory(ServerLevel level) {
			return new Factory<>(
					() -> new PersistentState(level),
					nbt -> new PersistentState(level, nbt),
					null // FAPI makes this fine
			);
		}
	}
}
