package io.github.fusionflux.portalcubed.framework.registration.block;

import net.minecraft.world.level.block.Block;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

public interface BlockFactory<T extends Block> {
	T create(QuiltBlockSettings settings);
}
