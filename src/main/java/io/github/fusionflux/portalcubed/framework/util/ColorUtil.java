package io.github.fusionflux.portalcubed.framework.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class ColorUtil {
	public static final List<Block> CONCRETE = List.of(
		Blocks.WHITE_CONCRETE,
		Blocks.ORANGE_CONCRETE,
		Blocks.MAGENTA_CONCRETE,
		Blocks.LIGHT_BLUE_CONCRETE,
		Blocks.YELLOW_CONCRETE,
		Blocks.LIME_CONCRETE,
		Blocks.PINK_CONCRETE,
		Blocks.GRAY_CONCRETE,
		Blocks.LIGHT_GRAY_CONCRETE,
		Blocks.CYAN_CONCRETE,
		Blocks.PURPLE_CONCRETE,
		Blocks.BLUE_CONCRETE,
		Blocks.BROWN_CONCRETE,
		Blocks.GREEN_CONCRETE,
		Blocks.RED_CONCRETE,
		Blocks.BLACK_CONCRETE
	);

	public static Block randomConcrete(RandomSource random) {
		return CONCRETE.get(random.nextInt(CONCRETE.size() - 1));
	}

	public static int getDyedColor(ItemStack stack) {
		return stack.getItem() instanceof DyeableLeatherItem dyeable ? dyeable.getColor(stack) : -1;
	}
}
