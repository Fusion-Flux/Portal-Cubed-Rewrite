package io.github.fusionflux.portalcubed.content.portal.manager;

import java.util.HashSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PlayerLookup;

import com.mojang.datafixers.util.Pair;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.UpdatePortalPairPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

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
	public void createPortal(String key, Polarity polarity, PortalData data) {
		this.modifyPair(key, pair -> pair.with(polarity, new PortalInstance(data)));
	}

	@Override
	public void setPair(String key, @Nullable PortalPair pair) {
		super.setPair(key, pair);
		UpdatePortalPairPacket packet = new UpdatePortalPairPacket(key, pair);
		PortalCubedPackets.sendToClients(PlayerLookup.world(this.level), packet);
	}

	public void removePortalsInBox(AABB bounds) {
		// TODO: this is a mess. Need a section-based lookup and easy removal
		// copy the ID set to avoid a CME
		for (String key : new HashSet<>(this.getAllKeys())) {
			this.modifyPair(key, pair -> {
				if (pair.primary().isPresent()) {
					PortalInstance primary = pair.primary().get();
					if (primary.renderBounds.intersects(bounds)) {
						pair = pair.without(Polarity.PRIMARY);
					}
				}
				if (pair.secondary().isPresent()) {
					PortalInstance secondary = pair.secondary().get();
					if (secondary.renderBounds.intersects(bounds)) {
						pair = pair.without(Polarity.SECONDARY);
					}
				}

				return pair;
			});
		}
	}

	public CompoundTag save(CompoundTag nbt) {
		if (this.portals.isEmpty())
			return nbt;

		CompoundTag portals = new CompoundTag();
		nbt.put("portals", portals);

		this.portals.forEach((key, pair) -> {
			Tag tag = PortalPair.CODEC.encodeStart(NbtOps.INSTANCE, pair).result().orElseThrow();
			portals.put(key, tag);
		});

		return nbt;
	}

	public void load(CompoundTag nbt) {
		CompoundTag portals = nbt.getCompound("portals");
		for (String key : portals.getAllKeys()) {
			Tag tag = portals.get(key);
			PortalPair.CODEC.decode(NbtOps.INSTANCE, tag).result().map(Pair::getFirst)
					.ifPresent(pair -> this.portals.put(key, pair));
		}
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
