package io.github.fusionflux.portalcubed.content.portal.manager;

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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

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
	public void setPair(UUID id, PortalPair pair) {
		super.setPair(id, pair);
		UpdatePortalPairPacket packet = new UpdatePortalPairPacket(id, pair);
		PortalCubedPackets.sendToClients(PlayerLookup.world(this.level), packet);
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
