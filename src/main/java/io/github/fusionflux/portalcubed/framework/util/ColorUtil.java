package io.github.fusionflux.portalcubed.framework.util;

import java.util.Optional;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;

public class ColorUtil {
	public static Optional<Block> randomConfettiBlock(RandomSource random) {
		return BuiltInRegistries.BLOCK.getRandomElementOf(PortalCubedBlockTags.CONFETTI, random).map(Holder::value);
	}
}
