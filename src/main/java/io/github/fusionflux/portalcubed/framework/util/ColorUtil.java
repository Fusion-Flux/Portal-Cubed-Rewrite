package io.github.fusionflux.portalcubed.framework.util;

import java.util.Optional;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ColorUtil {
	public static Optional<Block> randomConfettiBlock(RandomSource random) {
		return BuiltInRegistries.BLOCK.getTagOrEmpty(PortalCubedBlockTags.CONFETTI).map(tag -> tag.get(random.nextInt(tag.size() - 1)).value());
	}

	public static int getDyedColor(ItemStack stack) {
		return stack.has(DataComponents.DYED_COLOR) ? stack.get(DataComponents.DYED_COLOR).rgb() : -1;
	}
}
