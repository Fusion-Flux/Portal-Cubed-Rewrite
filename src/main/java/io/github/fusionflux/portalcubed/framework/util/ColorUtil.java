package io.github.fusionflux.portalcubed.framework.util;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class ColorUtil {
	public static Optional<Block> randomConfettiBlock(RandomSource random) {
		return BuiltInRegistries.BLOCK.getTag(PortalCubedBlockTags.CONFETTI).map(tag -> tag.get(random.nextInt(tag.size() - 1)).value());
	}

	public static int getDyedColor(ItemStack stack) {
		return stack.getItem() instanceof DyeableLeatherItem dyeable ? dyeable.getColor(stack) : -1;
	}
}
