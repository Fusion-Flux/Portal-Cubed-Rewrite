package io.github.fusionflux.portalcubed.framework.registration.block;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import net.minecraft.world.level.block.Block;

public interface BlockFactory<T extends Block> {
	T create(QuiltBlockSettings settings);
}
