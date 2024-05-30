package io.github.fusionflux.portalcubed.framework.particle;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class DecalParticleLightCache {
	// most decal particles will probably be grouped up, so this shouldn't be too big but not too tiny
	private static final int CACHE_SIZE = 256;

	private final Level world;
	private final Long2IntLinkedOpenHashMap cache;

	public DecalParticleLightCache(Level world) {
		this.world = world;
		this.cache = new Long2IntLinkedOpenHashMap(CACHE_SIZE, Hash.FAST_LOAD_FACTOR);
		this.cache.defaultReturnValue(-1);
	}

	public void prepare() {
		this.cache.clear();
	}

	public int get(double x, double y, double z) {
		BlockPos pos = BlockPos.containing(x, y, z);
		long key = pos.asLong();

		int value = this.cache.get(key);
		if (value == -1) {
			if (this.cache.size() >= CACHE_SIZE) this.cache.removeLastInt();
			this.cache.putAndMoveToFirst(key, value = LevelRenderer.getLightColor(world, pos));
		}

		return value;
	}
}
