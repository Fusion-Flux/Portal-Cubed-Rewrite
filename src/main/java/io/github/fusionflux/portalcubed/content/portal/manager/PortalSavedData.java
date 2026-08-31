package io.github.fusionflux.portalcubed.content.portal.manager;

import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.PortalChangeListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class PortalSavedData extends SavedData implements PortalChangeListener {
	public static final Identifier ID = PortalCubed.id("portals");
	public static final Codec<Map<String, PortalPair>> PAIR_MAP_CODEC = Codec.unboundedMap(Codec.STRING, PortalPair.CODEC);

	private static final Logger logger = LogUtils.getLogger();

	public final ServerPortalManager manager;

	// fresh instance
	public PortalSavedData(ServerLevel level) {
		this.manager = new ServerPortalManager(level);
		this.manager.listeners().registerPersistent(this);
	}

	// loaded from data
	public PortalSavedData(ServerLevel level, Map<String, PortalPair> pairs) {
		this(level);
		pairs.forEach(this.manager::setPair);
	}

	@Override
	public void portalPairChanged(PortalPair oldPair, PortalPair newPair) {
		this.setDirty();
	}

	// serialization - this setup is pretty jank because we need the level. FAPI does basically the same thing for attachments.

	private <T> DataResult<T> encode(DynamicOps<T> ops, T prefix) {
		return PAIR_MAP_CODEC.encodeStart(ops, this.manager.pairs());
	}

	public static SavedDataType<PortalSavedData> createType(ServerLevel level) {
		return createType(ID, () -> new PortalSavedData(level), createCodec(level));
	}

	private static Codec<PortalSavedData> createCodec(ServerLevel level) {
		return Codec.of(PortalSavedData::encode, new Decoder<>() {
					@Override
					public <T> DataResult<Pair<PortalSavedData, T>> decode(DynamicOps<T> ops, T input) {
						return PAIR_MAP_CODEC.decode(ops, input).map(pair -> pair.mapFirst(
								portalPairs -> new PortalSavedData(level, portalPairs)
						));
					}
				}
		);
	}

	@SuppressWarnings({"DataFlowIssue", "SameParameterValue"})
	private static <T extends SavedData> SavedDataType<T> createType(Identifier id, Supplier<T> constructor, Codec<T> codec) {
		// FAPI makes it so passing null here is fine
		return new SavedDataType<>(id, constructor, codec, null);
	}
}
